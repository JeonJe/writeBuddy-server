package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.config.OpenAiProperties
import com.writebuddy.writebuddy.domain.RealExample
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

data class Sextuple<out A, out B, out C, out D, out E, out F>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F
)

@Component
class OpenAiClient(
    private val restClient: RestClient,
    private val promptManager: PromptManager,
    private val openAiProperties: OpenAiProperties,
    private val responseParser: OpenAiResponseParser
) {
    private val logger: Logger = LoggerFactory.getLogger(OpenAiClient::class.java)

    fun generateCorrectionAndFeedbackWithType(origin: String): Triple<String, String, String> {
        val (corrected, feedback, type, _) = generateCorrectionAndFeedbackWithScore(origin)
        return Triple(corrected, feedback, type)
    }
    
    fun generateCorrectionAndFeedbackWithScore(origin: String): Quadruple<String, String, String, Int> {
        val (corrected, feedback, type, score, _, _) = generateCorrectionWithTranslations(origin)
        return Quadruple(corrected, feedback, type, score)
    }
    
    fun generateCorrectionWithTranslations(origin: String): Sextuple<String, String, String, Int, String?, String?> {
        return try {
            logger.info("Requesting OpenAI correction with translations for: {}", origin)
            
            val response = callOpenAiApi(origin)
            val content = response?.choices?.firstOrNull()?.message?.content ?: ""
            logger.info("OpenAI response received: {}", content.take(200))
            
            val (correctionData, _, success) = responseParser.parseIntegratedResponse(content)
            if (success) {
                logger.info("통합 JSON 응답 파싱 성공")
                correctionData
            } else {
                logger.info("통합 JSON 파싱 실패, 기존 방식으로 파싱")
                responseParser.parseResponseWithTranslations(content)
            }
            
        } catch (e: Exception) {
            logger.error("Error calling OpenAI API: {}", e.message, e)
            getFallbackResponseWithTranslations(origin)
        }
    }
    
    fun generateCorrectionWithExamples(origin: String): Triple<Sextuple<String, String, String, Int, String?, String?>, List<RealExample>, Boolean> {
        return try {
            logger.info("Requesting OpenAI correction with examples for: {}", origin)
            
            val response = callOpenAiApi(origin)
            val content = response?.choices?.firstOrNull()?.message?.content ?: ""
            logger.info("OpenAI response received: {}", content.take(200))
            
            responseParser.parseIntegratedResponse(content)
            
        } catch (e: Exception) {
            logger.error("Error calling OpenAI API: {}", e.message, e)
            val fallbackCorrection = getFallbackResponseWithTranslations(origin)
            Triple(fallbackCorrection, emptyList(), false)
        }
    }
    
    private fun callOpenAiApi(origin: String): ChatCompletionResponse? {
        val messages = createMessages(origin)
        val request = createRequest(messages)
        
        val startTime = System.currentTimeMillis()
        return try {
            val response = restClient.post()
                .uri(openAiProperties.api.endpoint)
                .body(request)
                .retrieve()
                .body(ChatCompletionResponse::class.java)
            
            val endTime = System.currentTimeMillis()
            logger.info("OpenAI API 호출 완료: {}ms", endTime - startTime)
            response
        } catch (e: Exception) {
            val endTime = System.currentTimeMillis()
            logger.error("OpenAI API 호출 실패: {}ms, 오류: {}", endTime - startTime, e.message)
            throw e
        }
    }
    
    private fun createMessages(origin: String): List<Map<String, String>> {
        return listOf(
            mapOf("role" to OpenAiResponseParser.ROLE_SYSTEM, "content" to promptManager.getCorrectionSystemPrompt()),
            mapOf("role" to OpenAiResponseParser.ROLE_USER, "content" to promptManager.getCorrectionUserPrompt(origin))
        )
    }
    
    private fun createRequest(messages: List<Map<String, String>>): Map<String, Any> {
        val request = mutableMapOf<String, Any>(
            "model" to openAiProperties.api.model,
            "messages" to messages
        )

        // GPT-5/o1/o3 models don't support temperature parameter
        val model = openAiProperties.api.model
        val isReasoningModel = model.startsWith("gpt-5") || model.startsWith("o1") || model.startsWith("o3")
        val temperature = openAiProperties.api.temperature

        if (!isReasoningModel && temperature != null) {
            request["temperature"] = temperature
        }

        return request
    }

    
    private fun getFallbackResponseWithTranslations(origin: String): Sextuple<String, String, String, Int, String?, String?> {
        logger.warn("Using fallback response with translations for: {}", origin)
        return Sextuple(origin, promptManager.getCorrectionFallback(), OpenAiResponseParser.FALLBACK_FEEDBACK_TYPE, OpenAiResponseParser.DEFAULT_SCORE, null, null)
    }
    
    fun sendChatRequest(systemPrompt: String, userPrompt: String): String {
        return try {
            logger.info("OpenAI API 요청 전송: {}", userPrompt.take(50))
            val response = callOpenAiWithCustomPrompts(systemPrompt, userPrompt)
            response?.choices?.firstOrNull()?.message?.content ?: ""
        } catch (e: Exception) {
            logger.error("OpenAI API 호출 실패: {}", e.message)
            throw e
        }
    }
    
    private fun callOpenAiWithCustomPrompts(systemPrompt: String, userPrompt: String): ChatCompletionResponse? {
        val messages = listOf(
            mapOf("role" to "system", "content" to systemPrompt),
            mapOf("role" to "user", "content" to userPrompt)
        )
        val request = createRequest(messages)
        
        return restClient.post()
            .uri(openAiProperties.api.endpoint)
            .body(request)
            .retrieve()
            .body(ChatCompletionResponse::class.java)
    }
    
    @Retryable(
        value = [RestClientException::class],
        maxAttempts = 3, // Will be overridden by @Value at runtime
        backoff = Backoff(delay = 1000L, multiplier = 2.0) // Will be overridden by @Value at runtime
    )
    fun generateChatResponse(question: String): String {
        return try {
            logger.info("Requesting OpenAI chat response for: {}", question)
            
            val response = callChatApi(question)
            val content = response?.choices?.firstOrNull()?.message?.content ?: ""
            logger.info("OpenAI chat response received: {}", content)
            
            content.ifBlank { 
                promptManager.getChatFallback()
            }
            
        } catch (e: Exception) {
            logger.error("Error calling OpenAI Chat API: {}", e.message, e)
            promptManager.getChatFallback()
        }
    }
    
    private fun callChatApi(question: String): ChatCompletionResponse? {
        val messages = createChatMessages(question)
        val request = createRequest(messages)
        
        return restClient.post()
            .uri(openAiProperties.api.endpoint)
            .body(request)
            .retrieve()
            .body(ChatCompletionResponse::class.java)
    }
    
    private fun createChatMessages(question: String): List<Map<String, String>> {
        return listOf(
            mapOf("role" to OpenAiResponseParser.ROLE_SYSTEM, "content" to promptManager.getChatSystemPrompt()),
            mapOf("role" to OpenAiResponseParser.ROLE_USER, "content" to question)
        )
    }
}

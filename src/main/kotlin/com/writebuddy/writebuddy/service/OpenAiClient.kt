package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.config.OpenAiProperties
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
            logger.info("OpenAI response received: {}", content)
            
            responseParser.parseResponseWithTranslations(content)
            
        } catch (e: Exception) {
            logger.error("Error calling OpenAI API: {}", e.message, e)
            getFallbackResponseWithTranslations(origin)
        }
    }
    
    private fun callOpenAiApi(origin: String): ChatCompletionResponse? {
        val messages = createMessages(origin)
        val request = createRequest(messages)
        
        return restClient.post()
            .uri(openAiProperties.api.endpoint)
            .body(request)
            .retrieve()
            .body(ChatCompletionResponse::class.java)
    }
    
    private fun createMessages(origin: String): List<Map<String, String>> {
        return listOf(
            mapOf("role" to OpenAiResponseParser.ROLE_SYSTEM, "content" to promptManager.getCorrectionSystemPrompt()),
            mapOf("role" to OpenAiResponseParser.ROLE_USER, "content" to promptManager.getCorrectionUserPrompt(origin))
        )
    }
    
    private fun createRequest(messages: List<Map<String, String>>): Map<String, Any> {
        return mapOf(
            "model" to openAiProperties.api.model,
            "messages" to messages,
            "temperature" to openAiProperties.api.temperature
        )
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

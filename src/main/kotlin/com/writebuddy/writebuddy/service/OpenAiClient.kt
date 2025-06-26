package com.writebuddy.writebuddy.service

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
    private val restClient: RestClient
) {
    private val logger: Logger = LoggerFactory.getLogger(OpenAiClient::class.java)
    
    companion object {
        private const val MODEL = "gpt-4o-mini"
        private const val TEMPERATURE = 0.3
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY = 1000L
        private const val RETRY_MULTIPLIER = 2.0
        
        private val SYSTEM_PROMPT = """당신은 영어 문법 교정 선생님입니다. 반드시 다음 형식으로 한국어로 응답해주세요:
교정문: [교정된 문장]
피드백: [한국어로 설명]
유형: [Grammar/Spelling/Style/Punctuation 중 하나]
점수: [1-10점, 1점=매우 나쁨, 10점=완벽함]
원문번역: [원문의 한국어 번역]
피드백번역: [피드백 내용의 영어 번역]""".trimIndent()
        
        private val CHAT_SYSTEM_PROMPT = """당신은 친절한 영어 학습 도우미입니다. 
사용자의 영어 관련 질문에 대해 한국어로 정확하고 이해하기 쉽게 답변해주세요.
문법, 표현, 단어 사용법, 문화적 뉘앙스 등 영어 학습에 도움이 되는 모든 정보를 제공해주세요.""".trimIndent()
        
        private const val USER_PROMPT_TEMPLATE = "다음 영어 문장을 교정하고 한국어로 설명해주세요:\n%s"
        private const val FALLBACK_FEEDBACK = "죄송합니다. 현재 교정 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요."
        
        // Response parsing constants
        private const val CORRECTED_PREFIX_KOR = "교정문:"
        private const val CORRECTED_PREFIX_ENG = "CORRECTED:"
        private const val FEEDBACK_PREFIX_KOR = "피드백:"
        private const val FEEDBACK_PREFIX_ENG = "FEEDBACK:"
        private const val TYPE_PREFIX_KOR = "유형:"
        private const val TYPE_PREFIX_ENG = "TYPE:"
        private const val SCORE_PREFIX_KOR = "점수:"
        private const val SCORE_PREFIX_ENG = "SCORE:"
        private const val ORIGIN_TRANSLATION_PREFIX_KOR = "원문번역:"
        private const val ORIGIN_TRANSLATION_PREFIX_ENG = "ORIGIN TRANSLATION:"
        private const val FEEDBACK_TRANSLATION_PREFIX_KOR = "피드백번역:"
        private const val FEEDBACK_TRANSLATION_PREFIX_ENG = "FEEDBACK TRANSLATION:"
        private const val DEFAULT_FEEDBACK_TYPE = "Grammar"
        private const val FALLBACK_FEEDBACK_TYPE = "SYSTEM"
        private const val DEFAULT_SCORE = 5
        
        // API constants
        private const val API_ENDPOINT = "/chat/completions"
        private const val ROLE_SYSTEM = "system"
        private const val ROLE_USER = "user"
    }

    @Retryable(
        value = [RestClientException::class],
        maxAttempts = MAX_RETRIES,
        backoff = Backoff(delay = RETRY_DELAY, multiplier = RETRY_MULTIPLIER)
    )
    fun generateCorrectionAndFeedback(origin: String): Pair<String, String> {
        val (corrected, feedback, _) = generateCorrectionAndFeedbackWithType(origin)
        return corrected to feedback
    }
    
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
            
            parseResponseWithTranslations(content)
            
        } catch (e: Exception) {
            logger.error("Error calling OpenAI API: {}", e.message, e)
            getFallbackResponseWithTranslations(origin)
        }
    }
    
    private fun callOpenAiApi(origin: String): ChatCompletionResponse? {
        val messages = createMessages(origin)
        val request = createRequest(messages)
        
        return restClient.post()
            .uri(API_ENDPOINT)
            .body(request)
            .retrieve()
            .body(ChatCompletionResponse::class.java)
    }
    
    private fun createMessages(origin: String): List<Map<String, String>> {
        return listOf(
            mapOf("role" to ROLE_SYSTEM, "content" to SYSTEM_PROMPT),
            mapOf("role" to ROLE_USER, "content" to USER_PROMPT_TEMPLATE.format(origin))
        )
    }
    
    private fun createRequest(messages: List<Map<String, String>>): Map<String, Any> {
        return mapOf(
            "model" to MODEL,
            "messages" to messages,
            "temperature" to TEMPERATURE
        )
    }

    private fun parseEnhancedResponseWithScore(content: String): Quadruple<String, String, String, Int> {
        val lines = content.lines()
        
        val corrected = extractValue(lines, CORRECTED_PREFIX_KOR, CORRECTED_PREFIX_ENG)
            ?: content.split("\n").firstOrNull()?.trim() ?: ""
        
        val feedback = extractValue(lines, FEEDBACK_PREFIX_KOR, FEEDBACK_PREFIX_ENG)
            ?: content.split("\n").drop(1).joinToString(" ").trim()
        
        val feedbackType = extractValue(lines, TYPE_PREFIX_KOR, TYPE_PREFIX_ENG) ?: DEFAULT_FEEDBACK_TYPE
        
        val scoreStr = extractValue(lines, SCORE_PREFIX_KOR, SCORE_PREFIX_ENG) ?: DEFAULT_SCORE.toString()
        val score = parseScore(scoreStr)
        
        return Quadruple(corrected, feedback, feedbackType, score)
    }
    
    private fun extractValue(lines: List<String>, primaryPrefix: String, secondaryPrefix: String): String? {
        return lines.find { it.startsWith(primaryPrefix) }?.removePrefix(primaryPrefix)?.trim()
            ?: lines.find { it.startsWith(secondaryPrefix) }?.removePrefix(secondaryPrefix)?.trim()
    }
    
    private fun parseScore(scoreStr: String): Int {
        return try {
            val cleanScore = scoreStr.replace(Regex("[^0-9]"), "")
            val score = cleanScore.toIntOrNull() ?: DEFAULT_SCORE
            score.coerceIn(1, 10)
        } catch (e: Exception) {
            logger.warn("Failed to parse score: {}, using default: {}", scoreStr, DEFAULT_SCORE)
            DEFAULT_SCORE
        }
    }
    
    private fun parseResponseWithTranslations(content: String): Sextuple<String, String, String, Int, String?, String?> {
        val lines = content.lines()
        
        val corrected = extractValue(lines, CORRECTED_PREFIX_KOR, CORRECTED_PREFIX_ENG)
            ?: content.split("\n").firstOrNull()?.trim() ?: ""
        
        val feedback = extractValue(lines, FEEDBACK_PREFIX_KOR, FEEDBACK_PREFIX_ENG)
            ?: content.split("\n").drop(1).joinToString(" ").trim()
        
        val feedbackType = extractValue(lines, TYPE_PREFIX_KOR, TYPE_PREFIX_ENG) ?: DEFAULT_FEEDBACK_TYPE
        
        val scoreStr = extractValue(lines, SCORE_PREFIX_KOR, SCORE_PREFIX_ENG) ?: DEFAULT_SCORE.toString()
        val score = parseScore(scoreStr)
        
        val originTranslation = extractValue(lines, ORIGIN_TRANSLATION_PREFIX_KOR, ORIGIN_TRANSLATION_PREFIX_ENG)
        val feedbackTranslation = extractValue(lines, FEEDBACK_TRANSLATION_PREFIX_KOR, FEEDBACK_TRANSLATION_PREFIX_ENG)
        
        return Sextuple(corrected, feedback, feedbackType, score, originTranslation, feedbackTranslation)
    }
    
    private fun getFallbackResponseWithScore(origin: String): Quadruple<String, String, String, Int> {
        logger.warn("Using fallback response with score for: {}", origin)
        return Quadruple(origin, FALLBACK_FEEDBACK, FALLBACK_FEEDBACK_TYPE, DEFAULT_SCORE)
    }
    
    private fun getFallbackResponseWithTranslations(origin: String): Sextuple<String, String, String, Int, String?, String?> {
        logger.warn("Using fallback response with translations for: {}", origin)
        return Sextuple(origin, FALLBACK_FEEDBACK, FALLBACK_FEEDBACK_TYPE, DEFAULT_SCORE, null, null)
    }
    
    @Retryable(
        value = [RestClientException::class],
        maxAttempts = MAX_RETRIES,
        backoff = Backoff(delay = RETRY_DELAY, multiplier = RETRY_MULTIPLIER)
    )
    fun generateChatResponse(question: String): String {
        return try {
            logger.info("Requesting OpenAI chat response for: {}", question)
            
            val response = callChatApi(question)
            val content = response?.choices?.firstOrNull()?.message?.content ?: ""
            logger.info("OpenAI chat response received: {}", content)
            
            if (content.isBlank()) {
                "죄송합니다. 답변을 생성할 수 없습니다. 다시 시도해주세요."
            } else {
                content
            }
            
        } catch (e: Exception) {
            logger.error("Error calling OpenAI Chat API: {}", e.message, e)
            "죄송합니다. 현재 채팅 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요."
        }
    }
    
    private fun callChatApi(question: String): ChatCompletionResponse? {
        val messages = createChatMessages(question)
        val request = createRequest(messages)
        
        return restClient.post()
            .uri(API_ENDPOINT)
            .body(request)
            .retrieve()
            .body(ChatCompletionResponse::class.java)
    }
    
    private fun createChatMessages(question: String): List<Map<String, String>> {
        return listOf(
            mapOf("role" to ROLE_SYSTEM, "content" to CHAT_SYSTEM_PROMPT),
            mapOf("role" to ROLE_USER, "content" to question)
        )
    }
}

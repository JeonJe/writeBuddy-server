package com.writebuddy.writebuddy.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.writebuddy.writebuddy.controller.dto.response.SentencePracticeResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PracticeService(
    private val fastAiChatService: FastAiChatService
) {
    private val logger: Logger = LoggerFactory.getLogger(PracticeService::class.java)
    private val objectMapper = jacksonObjectMapper()

    private val topics = listOf(
        "business meeting", "project deadline", "team collaboration",
        "coffee shop order", "restaurant reservation", "hotel check-in",
        "job interview", "salary negotiation", "vacation request",
        "bug report", "code review", "deployment issue",
        "customer complaint", "product feedback", "sales pitch",
        "travel planning", "shopping", "asking for directions"
    )

    fun generateSentencePractice(): SentencePracticeResponse {
        val randomTopic = topics.random()

        val systemPrompt = """
            You are an English learning assistant for Korean speakers.
            Generate a practical sentence for English writing practice.

            Response in JSON format only:
            {
                "korean": "Korean sentence to translate",
                "hint": "3-4 key English words as hints, comma separated",
                "bestAnswer": "Natural English translation"
            }

            Requirements:
            - Sentence must be about: $randomTopic
            - Practical and commonly used expression
            - Difficulty: intermediate level
            - Hints should guide without giving away the full answer
            - Be creative and generate a unique sentence
        """.trimIndent()

        val userPrompt = "Generate one English practice sentence about '$randomTopic'. Be creative!"

        return try {
            val response = fastAiChatService.generateWithCustomPrompt(systemPrompt, userPrompt)
            parseResponse(response)
        } catch (e: Exception) {
            logger.error("문장 생성 실패: {}", e.message, e)
            getFallbackSentence()
        }
    }

    private fun parseResponse(response: String): SentencePracticeResponse {
        val jsonContent = extractJson(response)
        return objectMapper.readValue(jsonContent)
    }

    private fun extractJson(response: String): String {
        val jsonStart = response.indexOf("{")
        val jsonEnd = response.lastIndexOf("}") + 1
        if (jsonStart == -1 || jsonEnd == 0) {
            throw IllegalArgumentException("JSON not found in response")
        }
        return response.substring(jsonStart, jsonEnd)
    }

    private fun getFallbackSentence(): SentencePracticeResponse {
        return SentencePracticeResponse(
            korean = "이 기능을 다음 스프린트에 추가할 수 있을까요?",
            hint = "add, feature, sprint",
            bestAnswer = "Could we add this feature to the next sprint?"
        )
    }
}

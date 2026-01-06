package com.writebuddy.writebuddy.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.writebuddy.writebuddy.controller.dto.response.AiGrammarResponse
import com.writebuddy.writebuddy.controller.dto.response.AiWordResponse
import com.writebuddy.writebuddy.controller.dto.response.ExampleSentence
import com.writebuddy.writebuddy.exception.ValidationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WordService(
    private val fastAiChatService: FastAiChatService,
    private val promptManager: PromptManager
) {
    private val logger: Logger = LoggerFactory.getLogger(WordService::class.java)
    private val objectMapper = jacksonObjectMapper()

    fun lookupWordWithAi(keyword: String): AiWordResponse {
        val normalized = normalizeKeyword(keyword)
        logger.info("AI word lookup requested: keyword={}", normalized)

        return try {
            val response = fastAiChatService.generateWithCustomPrompt(
                promptManager.getWordLookupSystemPrompt(),
                promptManager.getWordLookupUserPrompt(normalized)
            )
            parseAiResponse(response, normalized)
        } catch (e: Exception) {
            logger.error("AI word lookup failed: {}", e.message)
            AiWordResponse(
                word = normalized,
                meaning = "단어 정보를 가져오는 중 오류가 발생했습니다."
            )
        }
    }

    fun lookupGrammarWithAi(keyword: String): AiGrammarResponse {
        val normalized = normalizeKeyword(keyword)
        logger.info("AI grammar lookup requested: keyword={}", normalized)

        return try {
            val response = fastAiChatService.generateWithCustomPrompt(
                promptManager.getGrammarLookupSystemPrompt(),
                promptManager.getGrammarLookupUserPrompt(normalized)
            )
            parseGrammarResponse(response, normalized)
        } catch (e: Exception) {
            logger.error("AI grammar lookup failed: {}", e.message)
            AiGrammarResponse(
                expression = normalized,
                meaning = "문법 정보를 가져오는 중 오류가 발생했습니다."
            )
        }
    }

    private fun parseGrammarResponse(response: String, keyword: String): AiGrammarResponse {
        return try {
            val cleanedResponse = response
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val json = objectMapper.readValue<Map<String, Any>>(cleanedResponse)

            val correctMap = json["correct"] as? Map<*, *>
            val correct = correctMap?.let {
                ExampleSentence(
                    sentence = it["sentence"] as? String ?: "",
                    translation = it["translation"] as? String ?: ""
                )
            }

            AiGrammarResponse(
                expression = json["expression"] as? String ?: keyword,
                meaning = json["meaning"] as? String ?: "",
                correct = correct,
                wrong = json["wrong"] as? String,
                tip = json["tip"] as? String
            )
        } catch (e: Exception) {
            logger.error("Failed to parse grammar response: {}", e.message)
            AiGrammarResponse(
                expression = keyword,
                meaning = response
            )
        }
    }

    private fun parseAiResponse(response: String, keyword: String): AiWordResponse {
        return try {
            val cleanedResponse = response
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val json = objectMapper.readValue<Map<String, Any>>(cleanedResponse)

            val exampleMap = json["example"] as? Map<*, *>
            val example = exampleMap?.let {
                ExampleSentence(
                    sentence = it["sentence"] as? String ?: "",
                    translation = it["translation"] as? String ?: ""
                )
            }

            AiWordResponse(
                word = json["word"] as? String ?: keyword,
                meaning = json["meaning"] as? String ?: "",
                example = example,
                point = json["point"] as? String
            )
        } catch (e: Exception) {
            logger.error("Failed to parse AI response: {}", e.message)
            AiWordResponse(
                word = keyword,
                meaning = response
            )
        }
    }

    private fun normalizeKeyword(keyword: String): String {
        val trimmed = keyword.trim()
        if (trimmed.isBlank()) {
            throw ValidationException("keyword parameter is required")
        }
        return trimmed
    }
}

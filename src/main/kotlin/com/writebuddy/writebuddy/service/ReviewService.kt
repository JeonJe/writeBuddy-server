package com.writebuddy.writebuddy.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.writebuddy.writebuddy.controller.dto.request.CompareAnswerRequest
import com.writebuddy.writebuddy.controller.dto.request.SaveReviewRecordRequest
import com.writebuddy.writebuddy.controller.dto.response.CompareAnswerResponse
import com.writebuddy.writebuddy.controller.dto.response.ReviewSentenceResponse
import com.writebuddy.writebuddy.controller.dto.response.ReviewSentencesResponse
import com.writebuddy.writebuddy.controller.dto.response.SaveReviewRecordResponse
import com.writebuddy.writebuddy.domain.*
import com.writebuddy.writebuddy.repository.CorrectionRepository
import com.writebuddy.writebuddy.repository.ReviewRecordRepository
import com.writebuddy.writebuddy.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ReviewService(
    private val correctionRepository: CorrectionRepository,
    private val reviewRecordRepository: ReviewRecordRepository,
    private val userRepository: UserRepository,
    private val openAiClient: OpenAiClient,
    private val promptManager: PromptManager,
    private val objectMapper: ObjectMapper,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    private val logger = LoggerFactory.getLogger(ReviewService::class.java)

    /**
     * Get review sentences for a user
     * Returns favorite corrections converted to review sentences
     */
    @Transactional(readOnly = true)
    fun getReviewSentences(userId: Long, limit: Int): ReviewSentencesResponse {
        val favorites = correctionRepository.findFavoritesByUserId(userId)
        
        if (favorites.isEmpty()) {
            return ReviewSentencesResponse(sentences = emptyList(), total = 0)
        }

        val correctionIds = favorites.map { it.id }
        val reviewStats = reviewRecordRepository.findReviewStatsByUserIdAndCorrectionIds(userId, correctionIds)
            .associateBy { it.getCorrectionId() }

        val sentences = favorites.mapNotNull { correction ->
            val korean = correction.originTranslation ?: return@mapNotNull null
            val stats = reviewStats[correction.id]
            
            val lastReviewedAt = stats?.getLastReviewedAt()
            val reviewCount = stats?.getReviewCount()?.toInt() ?: 0
            val correctCount = stats?.getCorrectCount()?.toInt() ?: 0
            
            val nextReviewDate = if (stats != null && lastReviewedAt != null) {
                // Calculate based on last review result
                val lastRecord = reviewRecordRepository.findByUserIdAndCorrectionIdOrderByReviewDateDesc(userId, correction.id)
                    .firstOrNull()
                val wasCorrect = lastRecord?.isCorrect ?: false
                ReviewRecord.calculateNextReviewDate(wasCorrect, correctCount, lastReviewedAt.toLocalDate())
            } else {
                // Never reviewed - review today
                LocalDate.now(clock)
            }
            
            ReviewSentenceResponse(
                id = correction.id,
                korean = korean,
                hint = "",  // P1: AI hint generation
                bestAnswer = correction.correctedSentence,
                difficulty = ReviewDifficulty.MEDIUM,  // P2: AI difficulty classification
                lastReviewedAt = lastReviewedAt?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                reviewCount = reviewCount,
                nextReviewDate = nextReviewDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            )
        }
        
        // Sort: sentences due for review (nextReviewDate <= today) first, then by oldest nextReviewDate
        val today = LocalDate.now(clock)
        val sortedSentences = sentences.sortedWith(
            compareBy<ReviewSentenceResponse> { 
                val nextDate = LocalDate.parse(it.nextReviewDate)
                if (nextDate <= today) 0 else 1  // Due for review first
            }.thenBy { 
                LocalDate.parse(it.nextReviewDate)  // Then by date ascending
            }
        ).take(limit)

        return ReviewSentencesResponse(
            sentences = sortedSentences,
            total = favorites.size
        )
    }

    /**
     * Compare user answer with best answer using AI
     */
    fun compareAnswer(request: CompareAnswerRequest): CompareAnswerResponse {
        logger.info("Comparing answer for sentenceId: ${request.sentenceId}")
        
        val systemPrompt = promptManager.getAnswerCompareSystemPrompt()
        val userPrompt = promptManager.getAnswerCompareUserPrompt(
            korean = request.korean,
            userAnswer = request.userAnswer,
            bestAnswer = request.bestAnswer
        )
        
        return try {
            val response = openAiClient.sendChatRequest(systemPrompt, userPrompt)
            val comparison = parseAnswerComparison(response)
            CompareAnswerResponse.from(comparison)
        } catch (e: Exception) {
            logger.error("Failed to compare answer: ${e.message}", e)
            getFallbackComparisonResponse(request)
        }
    }

    private fun parseAnswerComparison(response: String): AnswerComparison {
        return try {
            // Try to extract JSON from response (handle markdown code blocks)
            val jsonContent = extractJsonFromResponse(response)
            val jsonNode = objectMapper.readTree(jsonContent)
            
            val differences = jsonNode["differences"]?.map { diffNode ->
                Difference(
                    type = DifferenceType.valueOf(diffNode["type"].asText().uppercase()),
                    userPart = diffNode["userPart"].asText(),
                    bestPart = diffNode["bestPart"].asText(),
                    explanation = diffNode["explanation"].asText(),
                    importance = Importance.valueOf(diffNode["importance"].asText().uppercase())
                )
            } ?: emptyList()
            
            AnswerComparison(
                isCorrect = jsonNode["isCorrect"].asBoolean(),
                score = jsonNode["score"].asInt(),
                differences = differences.take(3),  // Max 3 differences
                overallFeedback = jsonNode["overallFeedback"].asText(),
                tip = jsonNode["tip"].asText()
            )
        } catch (e: Exception) {
            logger.error("Failed to parse AI response: $response", e)
            throw e
        }
    }

    private fun extractJsonFromResponse(response: String): String {
        // Remove markdown code blocks if present
        val trimmed = response.trim()
        return when {
            trimmed.startsWith("```json") -> trimmed.removePrefix("```json").removeSuffix("```").trim()
            trimmed.startsWith("```") -> trimmed.removePrefix("```").removeSuffix("```").trim()
            else -> trimmed
        }
    }

    private fun getFallbackComparisonResponse(request: CompareAnswerRequest): CompareAnswerResponse {
        // Simple string comparison fallback
        val isExactMatch = request.userAnswer.trim().equals(request.bestAnswer.trim(), ignoreCase = true)
        val isSimilar = request.userAnswer.lowercase().contains(
            request.bestAnswer.lowercase().split(" ").take(3).joinToString(" ")
        )
        
        return CompareAnswerResponse(
            isCorrect = isExactMatch || isSimilar,
            score = if (isExactMatch) 100 else if (isSimilar) 70 else 50,
            differences = emptyList(),
            overallFeedback = if (isExactMatch) {
                "완벽해요! 모범 답안과 정확히 일치합니다."
            } else {
                "AI 분석이 일시적으로 불가능합니다. 모범 답안을 참고해주세요."
            },
            tip = "모범 답안: ${request.bestAnswer}"
        )
    }

    /**
     * Save review record and calculate next review date
     */
    @Transactional
    fun saveReviewRecord(userId: Long, request: SaveReviewRecordRequest): SaveReviewRecordResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }
        
        val reviewDate = LocalDateTime.parse(request.reviewDate, DateTimeFormatter.ISO_DATE_TIME)
        
        val reviewRecord = ReviewRecord(
            correctionId = request.sentenceId,
            userAnswer = request.userAnswer,
            isCorrect = request.isCorrect,
            score = request.score,
            timeSpent = request.timeSpent,
            reviewDate = reviewDate,
            user = user
        )
        
        reviewRecordRepository.save(reviewRecord)
        
        // Calculate next review date
        val correctCount = reviewRecordRepository.countCorrectByUserIdAndCorrectionId(userId, request.sentenceId)
        val nextReviewDate = ReviewRecord.calculateNextReviewDate(
            isCorrect = request.isCorrect,
            correctCount = correctCount,
            baseDate = reviewDate.toLocalDate()
        )
        
        logger.info("Saved review record for correction ${request.sentenceId}, next review: $nextReviewDate")
        
        return SaveReviewRecordResponse(
            success = true,
            nextReviewDate = nextReviewDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        )
    }
}

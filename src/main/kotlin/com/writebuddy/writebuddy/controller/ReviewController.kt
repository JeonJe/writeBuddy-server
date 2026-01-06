package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.request.CompareAnswerRequest
import com.writebuddy.writebuddy.controller.dto.request.SaveReviewRecordRequest
import com.writebuddy.writebuddy.controller.dto.response.CompareAnswerResponse
import com.writebuddy.writebuddy.controller.dto.response.ReviewSentencesResponse
import com.writebuddy.writebuddy.controller.dto.response.SaveReviewRecordResponse
import com.writebuddy.writebuddy.service.ReviewService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/review")
class ReviewController(
    private val reviewService: ReviewService
) {
    private val logger = LoggerFactory.getLogger(ReviewController::class.java)

    /**
     * P0-1: Get review sentences for the authenticated user
     * 
     * GET /api/review/sentences?limit=10
     */
    @GetMapping("/sentences")
    fun getReviewSentences(
        @AuthenticationPrincipal userId: Long?,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<ReviewSentencesResponse> {
        val effectiveUserId = userId ?: DEFAULT_USER_ID
        val effectiveLimit = limit.coerceIn(1, 50)
        
        logger.info("복습 문장 조회: userId=$effectiveUserId, limit=$effectiveLimit")
        
        val response = reviewService.getReviewSentences(effectiveUserId, effectiveLimit)
        return ResponseEntity.ok(response)
    }

    /**
     * P0-2: Compare user answer with best answer using AI
     * 
     * POST /api/review/compare
     * 
     * Note: This endpoint may take 2-5 seconds due to AI processing
     */
    @PostMapping("/compare")
    fun compareAnswer(
        @RequestBody @Valid request: CompareAnswerRequest
    ): ResponseEntity<CompareAnswerResponse> {
        logger.info("답변 비교 요청: sentenceId=${request.sentenceId}")
        
        val response = reviewService.compareAnswer(request)
        return ResponseEntity.ok(response)
    }

    /**
     * P0-3: Save review record and get next review date
     * 
     * POST /api/review/records
     */
    @PostMapping("/records")
    fun saveReviewRecord(
        @AuthenticationPrincipal userId: Long?,
        @RequestBody @Valid request: SaveReviewRecordRequest
    ): ResponseEntity<SaveReviewRecordResponse> {
        val effectiveUserId = userId ?: DEFAULT_USER_ID
        
        logger.info("복습 기록 저장: userId=$effectiveUserId, sentenceId=${request.sentenceId}")
        
        val response = reviewService.saveReviewRecord(effectiveUserId, request)
        return ResponseEntity.ok(response)
    }

    companion object {
        // Temporary default user ID while JWT auth is disabled
        private const val DEFAULT_USER_ID = 1L
    }
}

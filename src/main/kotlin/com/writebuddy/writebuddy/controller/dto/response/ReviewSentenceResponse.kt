package com.writebuddy.writebuddy.controller.dto.response

import com.writebuddy.writebuddy.domain.ReviewDifficulty

data class ReviewSentencesResponse(
    val sentences: List<ReviewSentenceResponse>,
    val total: Int
)

data class ReviewSentenceResponse(
    val id: Long,
    val korean: String,
    val hint: String,
    val bestAnswer: String,
    val difficulty: ReviewDifficulty,
    val lastReviewedAt: String?,  // ISO 8601 format or null
    val reviewCount: Int,
    val nextReviewDate: String    // YYYY-MM-DD format
)

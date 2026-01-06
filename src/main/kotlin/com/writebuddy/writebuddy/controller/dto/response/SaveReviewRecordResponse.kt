package com.writebuddy.writebuddy.controller.dto.response

data class SaveReviewRecordResponse(
    val success: Boolean,
    val nextReviewDate: String  // YYYY-MM-DD format
)

package com.writebuddy.writebuddy.controller.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class SaveReviewRecordRequest(
    @field:Positive(message = "sentenceId must be positive")
    val sentenceId: Long,
    
    @field:NotBlank(message = "userAnswer is required")
    val userAnswer: String,
    
    @field:NotNull(message = "isCorrect is required")
    val isCorrect: Boolean,
    
    @field:Min(0, message = "score must be between 0 and 100")
    @field:Max(100, message = "score must be between 0 and 100")
    val score: Int,
    
    @field:Min(0, message = "timeSpent must be non-negative")
    val timeSpent: Int,
    
    @field:NotBlank(message = "reviewDate is required")
    val reviewDate: String  // ISO 8601 format
)

package com.writebuddy.writebuddy.controller.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CompareAnswerRequest(
    @field:Positive(message = "sentenceId must be positive")
    val sentenceId: Long,
    
    @field:NotBlank(message = "userAnswer is required")
    val userAnswer: String,
    
    @field:NotBlank(message = "bestAnswer is required")
    val bestAnswer: String,
    
    @field:NotBlank(message = "korean is required")
    val korean: String
)

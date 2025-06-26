package com.writebuddy.writebuddy.controller.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChatRequest(
    @field:NotBlank(message = "질문은 필수입니다")
    @field:Size(min = 1, max = 2000, message = "질문은 1-2000자여야 합니다")
    val question: String
)
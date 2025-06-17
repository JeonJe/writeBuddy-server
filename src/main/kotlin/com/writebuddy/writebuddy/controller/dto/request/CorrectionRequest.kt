package com.writebuddy.writebuddy.controller.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CorrectionRequest(
    @field:NotBlank(message = "문장은 비어있을 수 없습니다")
    @field:Size(min = 1, max = 1000, message = "문장은 1자 이상 1000자 이하여야 합니다")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9\\s.,!?;:\"'()\\-]+$",
        message = "영문자, 숫자, 기본 구두점만 허용됩니다"
    )
    val originSentence: String
)

package com.writebuddy.writebuddy.controller.dto.request

import jakarta.validation.constraints.NotBlank

data class CorrectionRequest(
    @field:NotBlank val originSentence: String
)

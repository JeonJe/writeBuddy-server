package com.writebuddy.writebuddy.controller.dto.request

data class CorrectionRequest(
    val originSentence: String,
    val correctedSentence: String,
    val feedback: String,
)

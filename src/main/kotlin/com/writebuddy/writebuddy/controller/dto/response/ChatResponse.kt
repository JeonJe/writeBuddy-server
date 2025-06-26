package com.writebuddy.writebuddy.controller.dto.response

import java.time.LocalDateTime

data class ChatResponse(
    val question: String,
    val answer: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
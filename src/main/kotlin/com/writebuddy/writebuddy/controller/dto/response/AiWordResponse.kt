package com.writebuddy.writebuddy.controller.dto.response

data class AiWordResponse(
    val word: String,
    val meaning: String,
    val example: ExampleSentence? = null,
    val point: String? = null
)

data class ExampleSentence(
    val sentence: String,
    val translation: String
)

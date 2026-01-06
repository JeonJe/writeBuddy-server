package com.writebuddy.writebuddy.controller.dto.response

data class AiGrammarResponse(
    val expression: String,
    val meaning: String,
    val correct: ExampleSentence? = null,
    val wrong: String? = null,
    val tip: String? = null
)

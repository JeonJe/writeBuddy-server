package com.writebuddy.writebuddy.domain

data class AnswerComparison(
    val isCorrect: Boolean,
    val score: Int,
    val differences: List<Difference>,
    val overallFeedback: String,
    val tip: String
)

data class Difference(
    val type: DifferenceType,
    val userPart: String,
    val bestPart: String,
    val explanation: String,
    val importance: Importance
)

enum class DifferenceType {
    GRAMMAR,
    WORD_CHOICE,
    NATURALNESS,
    PUNCTUATION
}

enum class Importance {
    HIGH,
    MEDIUM,
    LOW
}

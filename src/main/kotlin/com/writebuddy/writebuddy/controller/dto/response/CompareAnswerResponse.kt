package com.writebuddy.writebuddy.controller.dto.response

import com.writebuddy.writebuddy.domain.AnswerComparison
import com.writebuddy.writebuddy.domain.Difference
import com.writebuddy.writebuddy.domain.DifferenceType
import com.writebuddy.writebuddy.domain.Importance

data class CompareAnswerResponse(
    val isCorrect: Boolean,
    val score: Int,
    val differences: List<DifferenceResponse>,
    val overallFeedback: String,
    val tip: String
) {
    companion object {
        fun from(comparison: AnswerComparison): CompareAnswerResponse {
            return CompareAnswerResponse(
                isCorrect = comparison.isCorrect,
                score = comparison.score,
                differences = comparison.differences.map { DifferenceResponse.from(it) },
                overallFeedback = comparison.overallFeedback,
                tip = comparison.tip
            )
        }
    }
}

data class DifferenceResponse(
    val type: DifferenceType,
    val userPart: String,
    val bestPart: String,
    val explanation: String,
    val importance: Importance
) {
    companion object {
        fun from(difference: Difference): DifferenceResponse {
            return DifferenceResponse(
                type = difference.type,
                userPart = difference.userPart,
                bestPart = difference.bestPart,
                explanation = difference.explanation,
                importance = difference.importance
            )
        }
    }
}

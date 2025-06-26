package com.writebuddy.writebuddy.controller.dto.response

import com.writebuddy.writebuddy.domain.Correction
import com.writebuddy.writebuddy.domain.FeedbackType
import com.writebuddy.writebuddy.domain.RealExample
import java.time.LocalDateTime

data class CorrectionResponse(
    val id: Long,
    val originSentence: String,
    val correctedSentence: String,
    val feedback: String,
    val feedbackType: FeedbackType,
    val score: Int?,
    val isFavorite: Boolean,
    val memo: String?,
    val createdAt: LocalDateTime,
    val originTranslation: String?,
    val correctedTranslation: String?,
    val relatedExamples: List<RealExampleResponse> = emptyList()
) {
    companion object {
        fun from(
            correction: Correction,
            examples: List<RealExample> = emptyList()
        ): CorrectionResponse {
            return CorrectionResponse(
                id = correction.id,
                originSentence = correction.originSentence,
                correctedSentence = correction.correctedSentence,
                feedback = correction.feedback,
                feedbackType = correction.feedbackType,
                score = correction.score,
                isFavorite = correction.isFavorite,
                memo = correction.memo,
                createdAt = correction.createdAt ?: LocalDateTime.now(),
                originTranslation = correction.originTranslation,
                correctedTranslation = correction.correctedTranslation,
                relatedExamples = examples.map { RealExampleResponse.from(it) }
            )
        }
    }
}

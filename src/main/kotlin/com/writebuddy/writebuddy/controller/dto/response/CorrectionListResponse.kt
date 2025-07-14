package com.writebuddy.writebuddy.controller.dto.response

import com.writebuddy.writebuddy.domain.FeedbackType
import com.writebuddy.writebuddy.repository.CorrectionLightProjection
import java.time.LocalDateTime

data class CorrectionListResponse(
    val id: Long,
    val originSentence: String,
    val correctedSentence: String,
    val feedbackType: FeedbackType,
    val score: Int?,
    val isFavorite: Boolean,
    val createdAt: LocalDateTime?
) {
    companion object {
        fun from(projection: CorrectionLightProjection): CorrectionListResponse {
            return CorrectionListResponse(
                id = projection.getId(),
                originSentence = projection.getOriginSentence(),
                correctedSentence = projection.getCorrectedSentence(),
                feedbackType = projection.getFeedbackType(),
                score = projection.getScore(),
                isFavorite = projection.getIsFavorite(),
                createdAt = projection.getCreatedAt()
            )
        }
    }
}
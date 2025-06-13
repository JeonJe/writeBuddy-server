package com.writebuddy.writebuddy.controller.dto.response

import com.writebuddy.writebuddy.domain.Correction
import com.writebuddy.writebuddy.domain.FeedbackType
import java.time.LocalDateTime

data class CorrectionResponse(
    val id: Long,
    val originSentence: String,
    val correctedSentence: String,
    val feedBack: String,
    val feedbackType: FeedbackType,
    val creatdAt: LocalDateTime
) {
    companion object {
        fun from(
            correction: Correction
        ): CorrectionResponse {
            return CorrectionResponse(
                id = correction.id,
                originSentence = correction.originSentence,
                correctedSentence = correction.correctedSentence,
                feedBack = correction.feedback,
                feedbackType = correction.feedbackType,
                creatdAt = correction.createdAt ?: LocalDateTime.now()
            )
        }
    }
}

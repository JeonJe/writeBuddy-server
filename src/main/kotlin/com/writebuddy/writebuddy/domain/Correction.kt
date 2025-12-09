package com.writebuddy.writebuddy.domain

import com.writebuddy.writebuddy.domain.common.BaseEntity
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(
    name = "corrections",
    indexes = [
        Index(name = "idx_correction_user_id", columnList = "user_id"),
        Index(name = "idx_correction_created_at", columnList = "created_at"),
        Index(name = "idx_correction_feedback_type", columnList = "feedback_type"),
        Index(name = "idx_correction_is_favorite", columnList = "is_favorite"),
        Index(name = "idx_correction_score", columnList = "score"),
        Index(name = "idx_correction_user_created", columnList = "user_id,created_at")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class Correction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val originSentence: String,
    val correctedSentence: String,
    @Lob
    val feedback: String,
    @Enumerated(EnumType.STRING)
    val feedbackType: FeedbackType = FeedbackType.GRAMMAR,
    val score: Int? = null,
    var isFavorite: Boolean = false,
    @Lob
    var memo: String? = null,
    @Lob
    val originTranslation: String? = null,
    @Lob
    val correctedTranslation: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,
) : BaseEntity() {

    fun toggleFavorite() {
        isFavorite = !isFavorite
    }

    fun updateMemo(newMemo: String?) {
        memo = newMemo
    }

    fun assignToUser(newUser: User) {
        user = newUser
    }

    fun hasScore(): Boolean = score != null

    fun checkIsFavorite(): Boolean = isFavorite
}

enum class FeedbackType {
    GRAMMAR,
    SPELLING,
    STYLE,
    PUNCTUATION,
    SYSTEM
}

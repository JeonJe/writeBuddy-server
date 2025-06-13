package com.writebuddy.writebuddy.domain

import com.writebuddy.writebuddy.domain.common.BaseEntity
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
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
) : BaseEntity()

enum class FeedbackType {
    GRAMMAR,     // 문법 교정
    SPELLING,    // 철자 교정
    STYLE,       // 스타일 개선
    PUNCTUATION, // 구두점 교정
    SYSTEM       // 시스템 피드백 (fallback)
}

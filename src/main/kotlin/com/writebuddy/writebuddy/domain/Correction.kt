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
    val errorType: ErrorType = ErrorType.GRAMMAR,
) : BaseEntity()

enum class ErrorType {
    GRAMMAR,     // 문법 오류
    SPELLING,    // 철자 오류
    STYLE,       // 스타일 개선
    PUNCTUATION, // 구두점 오류
    SYSTEM       // 시스템 오류 (fallback)
}

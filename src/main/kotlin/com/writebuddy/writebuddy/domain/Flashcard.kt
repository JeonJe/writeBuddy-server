package com.writebuddy.writebuddy.domain

import com.writebuddy.writebuddy.domain.common.BaseEntity
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "flashcards")
class Flashcard(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    val word: Word,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var memoryStatus: MemoryStatus = MemoryStatus.NEW,
    @Column(nullable = false)
    var reviewCount: Int = 0,
    @Column(nullable = false)
    var correctCount: Int = 0,
    @Column(nullable = false)
    var incorrectCount: Int = 0,
    var lastReviewedAt: LocalDateTime? = null,
    var nextReviewAt: LocalDateTime? = null,
    @Column(columnDefinition = "TEXT")
    var personalNote: String? = null,
    @Column(nullable = false)
    var isFavorite: Boolean = false
) : BaseEntity() {
    
    fun markAsCorrect() {
        reviewCount++
        correctCount++
        lastReviewedAt = LocalDateTime.now()
        updateMemoryStatus()
        calculateNextReview()
    }
    
    fun markAsIncorrect() {
        reviewCount++
        incorrectCount++
        lastReviewedAt = LocalDateTime.now()
        memoryStatus = when (memoryStatus) {
            MemoryStatus.MASTERED -> MemoryStatus.REVIEWING
            MemoryStatus.REVIEWING -> MemoryStatus.LEARNING
            MemoryStatus.LEARNING -> MemoryStatus.STRUGGLING
            MemoryStatus.STRUGGLING -> MemoryStatus.STRUGGLING
            MemoryStatus.NEW -> MemoryStatus.STRUGGLING
        }
        calculateNextReview()
    }
    
    private fun updateMemoryStatus() {
        val accuracy = if (reviewCount > 0) correctCount.toDouble() / reviewCount else 0.0
        
        memoryStatus = when {
            reviewCount >= 5 && accuracy >= 0.9 -> MemoryStatus.MASTERED
            reviewCount >= 3 && accuracy >= 0.7 -> MemoryStatus.REVIEWING
            reviewCount >= 1 && accuracy >= 0.5 -> MemoryStatus.LEARNING
            else -> MemoryStatus.STRUGGLING
        }
    }
    
    private fun calculateNextReview() {
        val now = LocalDateTime.now()
        nextReviewAt = when (memoryStatus) {
            MemoryStatus.NEW -> now.plusHours(1)
            MemoryStatus.STRUGGLING -> now.plusHours(4)
            MemoryStatus.LEARNING -> now.plusDays(1)
            MemoryStatus.REVIEWING -> now.plusDays(3)
            MemoryStatus.MASTERED -> now.plusWeeks(1)
        }
    }
    
    fun getAccuracy(): Double {
        return if (reviewCount > 0) correctCount.toDouble() / reviewCount else 0.0
    }
    
    fun isReadyForReview(): Boolean {
        return nextReviewAt?.isBefore(LocalDateTime.now()) ?: true
    }
}

enum class MemoryStatus {
    NEW,         // 새로운 단어
    STRUGGLING,  // 어려워하는 단어
    LEARNING,    // 학습 중인 단어
    REVIEWING,   // 복습 중인 단어
    MASTERED     // 숙달된 단어
}
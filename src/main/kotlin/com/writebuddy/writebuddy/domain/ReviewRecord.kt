package com.writebuddy.writebuddy.domain

import com.writebuddy.writebuddy.domain.common.BaseEntity
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "review_records",
    indexes = [
        Index(name = "idx_review_record_user_id", columnList = "user_id"),
        Index(name = "idx_review_record_correction_id", columnList = "correction_id"),
        Index(name = "idx_review_record_review_date", columnList = "review_date"),
        Index(name = "idx_review_record_user_correction", columnList = "user_id,correction_id")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class ReviewRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "correction_id", nullable = false)
    val correctionId: Long,

    @Column(columnDefinition = "TEXT", nullable = false)
    val userAnswer: String,

    @Column(nullable = false)
    val isCorrect: Boolean,

    @Column(nullable = false)
    val score: Int,

    @Column(nullable = false)
    val timeSpent: Int,  // seconds

    @Column(name = "review_date", nullable = false)
    val reviewDate: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
) : BaseEntity() {

    companion object {
        /**
         * Calculate next review date based on spaced repetition algorithm
         * 
         * Correct answers: 3 days -> 1 week -> 2 weeks -> 1 month
         * Wrong answers: today -> 1 day -> 3 days
         */
        fun calculateNextReviewDate(
            isCorrect: Boolean,
            correctCount: Int,
            baseDate: LocalDate = LocalDate.now()
        ): LocalDate {
            return if (isCorrect) {
                when (correctCount) {
                    0 -> baseDate.plusDays(3)      // First correct: 3 days
                    1 -> baseDate.plusWeeks(1)    // Second correct: 1 week
                    2 -> baseDate.plusWeeks(2)    // Third correct: 2 weeks
                    else -> baseDate.plusMonths(1) // After: 1 month
                }
            } else {
                when (correctCount) {
                    0 -> baseDate                  // First wrong: today
                    1 -> baseDate.plusDays(1)     // Second wrong: 1 day
                    else -> baseDate.plusDays(3)  // After: 3 days
                }
            }
        }
    }
}

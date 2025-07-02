package com.writebuddy.writebuddy.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("Flashcard 도메인 테스트")
class FlashcardTest {

    @Nested
    @DisplayName("리뷰 결과 처리")
    inner class ReviewResultProcessing {

        @Test
        @DisplayName("정답 처리 시 카운트가 증가하고 상태가 업데이트된다")
        fun markAsCorrect_shouldIncrementCountsAndUpdateStatus() {
            val user = createTestUser()
            val word = createTestWord()
            val flashcard = Flashcard(user = user, word = word)

            flashcard.markAsCorrect()

            assertThat(flashcard.reviewCount).isEqualTo(1)
            assertThat(flashcard.correctCount).isEqualTo(1)
            assertThat(flashcard.incorrectCount).isEqualTo(0)
            assertThat(flashcard.lastReviewedAt).isNotNull()
            assertThat(flashcard.nextReviewAt).isNotNull()
        }

        @Test
        @DisplayName("오답 처리 시 카운트가 증가하고 상태가 하락한다")
        fun markAsIncorrect_shouldIncrementCountsAndDegradeStatus() {
            val user = createTestUser()
            val word = createTestWord()
            val flashcard = Flashcard(user = user, word = word, memoryStatus = MemoryStatus.LEARNING)

            flashcard.markAsIncorrect()

            assertThat(flashcard.reviewCount).isEqualTo(1)
            assertThat(flashcard.correctCount).isEqualTo(0)
            assertThat(flashcard.incorrectCount).isEqualTo(1)
            assertThat(flashcard.memoryStatus).isEqualTo(MemoryStatus.STRUGGLING)
            assertThat(flashcard.lastReviewedAt).isNotNull()
            assertThat(flashcard.nextReviewAt).isNotNull()
        }

        @Test
        @DisplayName("연속 정답 시 메모리 상태가 단계적으로 향상된다")
        fun multipleCorrectAnswers_shouldProgressivelyImproveMemoryStatus() {
            val user = createTestUser()
            val word = createTestWord()
            val flashcard = Flashcard(user = user, word = word)

            // 첫 번째 정답
            flashcard.markAsCorrect()
            assertThat(flashcard.reviewCount).isEqualTo(1)
            assertThat(flashcard.correctCount).isEqualTo(1)

            // 추가 정답들로 상태 향상
            repeat(4) { flashcard.markAsCorrect() }
            
            assertThat(flashcard.reviewCount).isEqualTo(5)
            assertThat(flashcard.correctCount).isEqualTo(5)
            assertThat(flashcard.getAccuracy()).isEqualTo(1.0)
            assertThat(flashcard.memoryStatus).isEqualTo(MemoryStatus.MASTERED)
        }
    }

    @Nested
    @DisplayName("정확도 계산")
    inner class AccuracyCalculation {

        @Test
        @DisplayName("리뷰하지 않은 상태에서 정확도는 0이다")
        fun noReviews_accuracyShouldBeZero() {
            val user = createTestUser()
            val word = createTestWord()
            val flashcard = Flashcard(user = user, word = word)

            assertThat(flashcard.getAccuracy()).isEqualTo(0.0)
        }

        @Test
        @DisplayName("모든 답이 정답인 경우 정확도는 1.0이다")
        fun allCorrect_accuracyShouldBeOne() {
            val user = createTestUser()
            val word = createTestWord()
            val flashcard = Flashcard(user = user, word = word)

            repeat(3) { flashcard.markAsCorrect() }

            assertThat(flashcard.getAccuracy()).isEqualTo(1.0)
        }

        @Test
        @DisplayName("절반 정답인 경우 정확도는 0.5이다")
        fun halfCorrect_accuracyShouldBeHalf() {
            val user = createTestUser()
            val word = createTestWord()
            val flashcard = Flashcard(user = user, word = word)

            repeat(2) { flashcard.markAsCorrect() }
            repeat(2) { flashcard.markAsIncorrect() }

            assertThat(flashcard.getAccuracy()).isEqualTo(0.5)
        }
    }

    @Nested
    @DisplayName("복습 준비 상태")
    inner class ReviewReadiness {

        @Test
        @DisplayName("nextReviewAt이 null인 경우 복습 준비 상태이다")
        fun nullNextReviewAt_shouldBeReadyForReview() {
            val user = createTestUser()
            val word = createTestWord()
            val flashcard = Flashcard(user = user, word = word)
            flashcard.nextReviewAt = null

            assertThat(flashcard.isReadyForReview()).isTrue()
        }

        @Test
        @DisplayName("nextReviewAt이 현재 시간보다 이전인 경우 복습 준비 상태이다")
        fun pastNextReviewAt_shouldBeReadyForReview() {
            val user = createTestUser()
            val word = createTestWord()
            val flashcard = Flashcard(user = user, word = word)
            flashcard.nextReviewAt = LocalDateTime.now().minusHours(1)

            assertThat(flashcard.isReadyForReview()).isTrue()
        }

        @Test
        @DisplayName("nextReviewAt이 현재 시간보다 미래인 경우 복습 준비 상태가 아니다")
        fun futureNextReviewAt_shouldNotBeReadyForReview() {
            val user = createTestUser()
            val word = createTestWord()
            val flashcard = Flashcard(user = user, word = word)
            flashcard.nextReviewAt = LocalDateTime.now().plusHours(1)

            assertThat(flashcard.isReadyForReview()).isFalse()
        }
    }

    private fun createTestUser(): User {
        return User(
            id = 1L,
            username = "testuser",
            email = "test@example.com"
        )
    }

    private fun createTestWord(): Word {
        return Word(
            id = 1L,
            word = "test",
            meaning = "테스트",
            difficulty = 5,
            tags = mutableSetOf("test", "example"),
            category = WordCategory.GENERAL
        )
    }
}
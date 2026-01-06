package com.writebuddy.writebuddy.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("ReviewRecord 도메인 테스트")
class ReviewRecordTest {

    // 기준 날짜: 2025-12-25 (크리스마스)
    private val baseDate = LocalDate.of(2025, 12, 25)

    @Nested
    @DisplayName("간격 반복 알고리즘 - 정답인 경우")
    inner class CorrectAnswerSpacedRepetition {

        @Test
        @DisplayName("첫 번째 정답: 3일 후 복습")
        fun calculateNextReviewDate_firstCorrect_returns3DaysLater() {
            val nextDate = ReviewRecord.calculateNextReviewDate(
                isCorrect = true,
                correctCount = 0,
                baseDate = baseDate
            )

            assertThat(nextDate).isEqualTo(LocalDate.of(2025, 12, 28))
        }

        @Test
        @DisplayName("두 번째 정답: 1주 후 복습")
        fun calculateNextReviewDate_secondCorrect_returns1WeekLater() {
            val nextDate = ReviewRecord.calculateNextReviewDate(
                isCorrect = true,
                correctCount = 1,
                baseDate = baseDate
            )

            assertThat(nextDate).isEqualTo(LocalDate.of(2026, 1, 1))
        }

        @Test
        @DisplayName("세 번째 정답: 2주 후 복습")
        fun calculateNextReviewDate_thirdCorrect_returns2WeeksLater() {
            val nextDate = ReviewRecord.calculateNextReviewDate(
                isCorrect = true,
                correctCount = 2,
                baseDate = baseDate
            )

            assertThat(nextDate).isEqualTo(LocalDate.of(2026, 1, 8))
        }

        @Test
        @DisplayName("네 번째 이상 정답: 1개월 후 복습")
        fun calculateNextReviewDate_fourthOrMoreCorrect_returns1MonthLater() {
            val nextDate = ReviewRecord.calculateNextReviewDate(
                isCorrect = true,
                correctCount = 3,
                baseDate = baseDate
            )

            assertThat(nextDate).isEqualTo(LocalDate.of(2026, 1, 25))
        }

        @Test
        @DisplayName("다섯 번째 정답도 1개월 후 복습")
        fun calculateNextReviewDate_fifthCorrect_returns1MonthLater() {
            val nextDate = ReviewRecord.calculateNextReviewDate(
                isCorrect = true,
                correctCount = 4,
                baseDate = baseDate
            )

            assertThat(nextDate).isEqualTo(LocalDate.of(2026, 1, 25))
        }
    }

    @Nested
    @DisplayName("간격 반복 알고리즘 - 오답인 경우")
    inner class WrongAnswerSpacedRepetition {

        @Test
        @DisplayName("첫 번째 오답: 오늘 다시 복습")
        fun calculateNextReviewDate_firstWrong_returnsToday() {
            val nextDate = ReviewRecord.calculateNextReviewDate(
                isCorrect = false,
                correctCount = 0,
                baseDate = baseDate
            )

            assertThat(nextDate).isEqualTo(baseDate)
        }

        @Test
        @DisplayName("두 번째 오답: 1일 후 복습")
        fun calculateNextReviewDate_secondWrong_returns1DayLater() {
            val nextDate = ReviewRecord.calculateNextReviewDate(
                isCorrect = false,
                correctCount = 1,
                baseDate = baseDate
            )

            assertThat(nextDate).isEqualTo(LocalDate.of(2025, 12, 26))
        }

        @Test
        @DisplayName("세 번째 이상 오답: 3일 후 복습")
        fun calculateNextReviewDate_thirdOrMoreWrong_returns3DaysLater() {
            val nextDate = ReviewRecord.calculateNextReviewDate(
                isCorrect = false,
                correctCount = 2,
                baseDate = baseDate
            )

            assertThat(nextDate).isEqualTo(LocalDate.of(2025, 12, 28))
        }

        @Test
        @DisplayName("네 번째 오답도 3일 후 복습")
        fun calculateNextReviewDate_fourthWrong_returns3DaysLater() {
            val nextDate = ReviewRecord.calculateNextReviewDate(
                isCorrect = false,
                correctCount = 3,
                baseDate = baseDate
            )

            assertThat(nextDate).isEqualTo(LocalDate.of(2025, 12, 28))
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    inner class EdgeCases {

        @Test
        @DisplayName("월말 날짜에서 1개월 추가 시 다음달 말일로")
        fun calculateNextReviewDate_endOfMonth_handlesCorrectly() {
            // 2025-01-31에서 1개월 추가
            val endOfJanuary = LocalDate.of(2025, 1, 31)
            
            val nextDate = ReviewRecord.calculateNextReviewDate(
                isCorrect = true,
                correctCount = 3,
                baseDate = endOfJanuary
            )

            // 2월은 28일까지만 있으므로 2025-02-28
            assertThat(nextDate).isEqualTo(LocalDate.of(2025, 2, 28))
        }

        @Test
        @DisplayName("연말에서 1주 추가 시 다음해로 넘어감")
        fun calculateNextReviewDate_endOfYear_crossesYear() {
            val endOfYear = LocalDate.of(2025, 12, 29)
            
            val nextDate = ReviewRecord.calculateNextReviewDate(
                isCorrect = true,
                correctCount = 1,
                baseDate = endOfYear
            )

            assertThat(nextDate).isEqualTo(LocalDate.of(2026, 1, 5))
        }
    }
}

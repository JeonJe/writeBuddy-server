package com.writebuddy.writebuddy.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("RefreshToken 도메인 테스트")
class RefreshTokenTest {

    @Nested
    @DisplayName("isExpired 기본 동작")
    inner class IsExpiredBasicBehavior {

        @Test
        fun `isExpired_만료_시간_이전_false_반환`() {
            val expiryDate = LocalDateTime.of(2026, 1, 1, 0, 0, 0)
            val refreshToken = RefreshToken(
                token = "test-token",
                userId = 1L,
                expiryDate = expiryDate
            )

            assertThat(refreshToken.isExpired()).isFalse
        }

        @Test
        fun `isExpired_만료_시간_이후_true_반환`() {
            val expiryDate = LocalDateTime.of(2024, 1, 1, 0, 0, 0)
            val refreshToken = RefreshToken(
                token = "test-token",
                userId = 1L,
                expiryDate = expiryDate
            )

            assertThat(refreshToken.isExpired()).isTrue
        }

        @Test
        fun `isExpired_과거_시간_true_반환`() {
            val pastTime = LocalDateTime.of(2024, 12, 1, 10, 0, 0)
            val refreshToken = RefreshToken(
                token = "test-token",
                userId = 1L,
                expiryDate = pastTime
            )

            assertThat(refreshToken.isExpired()).isTrue
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    inner class BoundaryValueTests {

        @Test
        fun `isExpired_1년_후_만료_false_반환`() {
            val futureTime = LocalDateTime.now().plusYears(1)
            val refreshToken = RefreshToken(
                token = "test-token",
                userId = 1L,
                expiryDate = futureTime
            )

            assertThat(refreshToken.isExpired()).isFalse
        }

        @Test
        fun `isExpired_1년_전_만료_true_반환`() {
            val pastTime = LocalDateTime.now().minusYears(1)
            val refreshToken = RefreshToken(
                token = "test-token",
                userId = 1L,
                expiryDate = pastTime
            )

            assertThat(refreshToken.isExpired()).isTrue
        }

        @Test
        fun `isExpired_하루_후_만료_false_반환`() {
            val oneDayLater = LocalDateTime.now().plusDays(1)
            val refreshToken = RefreshToken(
                token = "test-token",
                userId = 1L,
                expiryDate = oneDayLater
            )

            assertThat(refreshToken.isExpired()).isFalse
        }

        @Test
        fun `isExpired_하루_전_만료_true_반환`() {
            val oneDayBefore = LocalDateTime.now().minusDays(1)
            val refreshToken = RefreshToken(
                token = "test-token",
                userId = 1L,
                expiryDate = oneDayBefore
            )

            assertThat(refreshToken.isExpired()).isTrue
        }

        @Test
        fun `isExpired_1초_후_만료_false_반환`() {
            val oneSecondLater = LocalDateTime.now().plusSeconds(1)
            val refreshToken = RefreshToken(
                token = "test-token",
                userId = 1L,
                expiryDate = oneSecondLater
            )

            assertThat(refreshToken.isExpired()).isFalse
        }

        @Test
        fun `isExpired_1초_전_만료_true_반환`() {
            val oneSecondBefore = LocalDateTime.now().minusSeconds(1)
            val refreshToken = RefreshToken(
                token = "test-token",
                userId = 1L,
                expiryDate = oneSecondBefore
            )

            assertThat(refreshToken.isExpired()).isTrue
        }
    }

    @Nested
    @DisplayName("밀리초 정밀도 테스트")
    inner class MillisecondPrecisionTests {

        @Test
        fun `isExpired_나노초_단위_미래_false_반환`() {
            val futureTime = LocalDateTime.now().plusNanos(1_000_000)
            val refreshToken = RefreshToken(
                token = "test-token",
                userId = 1L,
                expiryDate = futureTime
            )

            assertThat(refreshToken.isExpired()).isFalse
        }

        @Test
        fun `isExpired_나노초_단위_과거_true_반환`() {
            val pastTime = LocalDateTime.now().minusNanos(1_000_000)
            val refreshToken = RefreshToken(
                token = "test-token",
                userId = 1L,
                expiryDate = pastTime
            )

            assertThat(refreshToken.isExpired()).isTrue
        }

        @Test
        fun `토큰_생성_시_나노초_정밀도_유지`() {
            val preciseTime = LocalDateTime.of(2026, 6, 15, 14, 30, 45, 123_456_789)
            val refreshToken = RefreshToken(
                token = "test-token",
                userId = 1L,
                expiryDate = preciseTime
            )

            assertThat(refreshToken.expiryDate).isEqualTo(preciseTime)
            assertThat(refreshToken.expiryDate.nano).isEqualTo(123_456_789)
        }
    }

    @Nested
    @DisplayName("토큰 속성 테스트")
    inner class TokenPropertiesTests {

        @Test
        fun `토큰_생성_시_속성_정확히_설정`() {
            val expiryDate = LocalDateTime.of(2026, 1, 1, 0, 0, 0)
            val refreshToken = RefreshToken(
                token = "test-token-123",
                userId = 42L,
                expiryDate = expiryDate
            )

            assertThat(refreshToken.token).isEqualTo("test-token-123")
            assertThat(refreshToken.userId).isEqualTo(42L)
            assertThat(refreshToken.expiryDate).isEqualTo(expiryDate)
            assertThat(refreshToken.id).isEqualTo(0L)
        }

        @Test
        fun `긴_토큰_문자열_처리`() {
            val longToken = "a".repeat(500)
            val refreshToken = RefreshToken(
                token = longToken,
                userId = 1L,
                expiryDate = LocalDateTime.of(2026, 1, 1, 0, 0, 0)
            )

            assertThat(refreshToken.token).hasSize(500)
            assertThat(refreshToken.token).isEqualTo(longToken)
        }
    }
}
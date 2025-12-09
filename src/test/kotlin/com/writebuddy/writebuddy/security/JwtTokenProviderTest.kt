package com.writebuddy.writebuddy.security

import com.writebuddy.writebuddy.config.JwtProperties
import com.writebuddy.writebuddy.domain.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("JwtTokenProvider 테스트")
class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        val properties = JwtProperties(
            secret = "test-secret-key-for-jwt-token-generation-minimum-256-bits",
            accessTokenValidity = 3600000,  // 1시간
            refreshTokenValidity = 604800000,  // 7일
            issuer = "writebuddy-test"
        )
        jwtTokenProvider = JwtTokenProvider(properties)
    }

    @Nested
    @DisplayName("토큰 생성")
    inner class TokenGeneration {

        @Test
        fun `generateAccessToken_유효한_토큰_생성`() {
            val userId = 1L
            val email = "test@example.com"
            val roles = setOf(UserRole.USER)

            val token = jwtTokenProvider.generateAccessToken(userId, email, roles)

            assertThat(token).isNotEmpty
            assertThat(jwtTokenProvider.validateToken(token)).isTrue
        }

        @Test
        fun `generateRefreshToken_유효한_토큰_생성`() {
            val userId = 1L

            val token = jwtTokenProvider.generateRefreshToken(userId)

            assertThat(token).isNotEmpty
            assertThat(jwtTokenProvider.validateToken(token)).isTrue
        }

        @Test
        fun `getUserIdFromToken_토큰에서_사용자_ID_추출`() {
            val userId = 123L
            val token = jwtTokenProvider.generateAccessToken(
                userId, "test@example.com", setOf(UserRole.USER)
            )

            val extractedId = jwtTokenProvider.getUserIdFromToken(token)

            assertThat(extractedId).isEqualTo(userId)
        }

        @Test
        fun `getEmailFromToken_토큰에서_이메일_추출`() {
            val email = "user@example.com"
            val token = jwtTokenProvider.generateAccessToken(
                1L, email, setOf(UserRole.USER)
            )

            val extractedEmail = jwtTokenProvider.getEmailFromToken(token)

            assertThat(extractedEmail).isEqualTo(email)
        }

        @Test
        fun `getRolesFromToken_토큰에서_역할_추출`() {
            val roles = setOf(UserRole.USER, UserRole.ADMIN)
            val token = jwtTokenProvider.generateAccessToken(
                1L, "admin@example.com", roles
            )

            val extractedRoles = jwtTokenProvider.getRolesFromToken(token)

            assertThat(extractedRoles).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN)
        }
    }

    @Nested
    @DisplayName("토큰 검증")
    inner class TokenValidation {

        @Test
        fun `validateToken_유효한_토큰_true_반환`() {
            val token = jwtTokenProvider.generateAccessToken(
                1L, "test@example.com", setOf(UserRole.USER)
            )

            val isValid = jwtTokenProvider.validateToken(token)

            assertThat(isValid).isTrue
        }

        @Test
        fun `validateToken_잘못된_토큰_false_반환`() {
            val invalidToken = "invalid.jwt.token"

            val isValid = jwtTokenProvider.validateToken(invalidToken)

            assertThat(isValid).isFalse
        }

        @Test
        fun `validateToken_빈_토큰_false_반환`() {
            val isValid = jwtTokenProvider.validateToken("")

            assertThat(isValid).isFalse
        }
    }
}
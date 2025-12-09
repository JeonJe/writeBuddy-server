package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.controller.dto.response.TokenResponse
import com.writebuddy.writebuddy.domain.RefreshToken
import com.writebuddy.writebuddy.exception.AuthenticationException
import com.writebuddy.writebuddy.repository.RefreshTokenRepository
import com.writebuddy.writebuddy.repository.UserRepository
import com.writebuddy.writebuddy.security.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    /**
     * 이메일과 비밀번호 기반 로그인
     */
    @Transactional
    fun login(email: String, password: String): TokenResponse {
        val user = userRepository.findByEmail(email)
            ?: throw AuthenticationException("사용자를 찾을 수 없습니다: $email")

        // 비밀번호 검증
        if (user.password == null || !passwordEncoder.matches(password, user.password)) {
            throw AuthenticationException("비밀번호가 일치하지 않습니다")
        }

        val accessToken = jwtTokenProvider.generateAccessToken(
            user.id, user.email, user.roles
        )
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id)

        // 기존 Refresh Token 삭제 후 새로 저장
        refreshTokenRepository.deleteByUserId(user.id)
        saveRefreshToken(user.id, refreshToken)

        logger.info("로그인 성공: userId={}, email={}", user.id, user.email)

        return TokenResponse(accessToken, refreshToken)
    }

    /**
     * Refresh Token으로 Access Token 갱신
     */
    @Transactional
    fun refreshAccessToken(refreshToken: String): TokenResponse {
        require(jwtTokenProvider.validateToken(refreshToken)) { "유효하지 않은 Refresh Token입니다" }

        val userId = jwtTokenProvider.getUserIdFromToken(refreshToken)
        val savedToken = refreshTokenRepository.findByUserId(userId)
            ?: throw IllegalArgumentException("Refresh Token을 찾을 수 없습니다")

        if (savedToken.token != refreshToken) {
            throw IllegalArgumentException("일치하지 않는 Refresh Token입니다")
        }

        if (savedToken.isExpired()) {
            refreshTokenRepository.deleteByUserId(userId)
            throw IllegalArgumentException("만료된 Refresh Token입니다")
        }

        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다")
        }

        val newAccessToken = jwtTokenProvider.generateAccessToken(
            user.id, user.email, user.roles
        )

        logger.info("Access Token 갱신 성공: userId={}", userId)

        return TokenResponse(newAccessToken, refreshToken)
    }

    /**
     * 로그아웃
     */
    @Transactional
    fun logout(userId: Long) {
        refreshTokenRepository.deleteByUserId(userId)
        logger.info("로그아웃 성공: userId={}", userId)
    }

    private fun saveRefreshToken(userId: Long, token: String) {
        val expiryDate = LocalDateTime.now().plusSeconds(
            jwtTokenProvider.jwtProperties.refreshTokenValidity / 1000
        )

        val refreshToken = RefreshToken(
            token = token,
            userId = userId,
            expiryDate = expiryDate
        )

        refreshTokenRepository.save(refreshToken)
    }
}

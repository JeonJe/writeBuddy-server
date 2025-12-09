package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.request.LoginRequest
import com.writebuddy.writebuddy.controller.dto.request.RefreshTokenRequest
import com.writebuddy.writebuddy.controller.dto.response.TokenResponse
import com.writebuddy.writebuddy.service.AuthService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    /**
     * 이메일과 비밀번호 기반 로그인
     */
    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): TokenResponse {
        logger.info("로그인 요청: email={}", request.email)
        return authService.login(request.email, request.password)
    }

    /**
     * Access Token 갱신
     */
    @PostMapping("/refresh")
    fun refresh(@RequestBody @Valid request: RefreshTokenRequest): TokenResponse {
        logger.info("토큰 갱신 요청")
        return authService.refreshAccessToken(request.refreshToken)
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    fun logout(@AuthenticationPrincipal userId: Long?): ResponseEntity<Void> {
        if (userId != null) {
            logger.info("로그아웃 요청: userId={}", userId)
            authService.logout(userId)
        }
        return ResponseEntity.noContent().build()
    }
}
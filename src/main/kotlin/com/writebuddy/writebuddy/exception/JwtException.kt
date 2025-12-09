package com.writebuddy.writebuddy.exception

/**
 * JWT 관련 예외 클래스
 */
sealed class JwtException(message: String) : RuntimeException(message)

class InvalidTokenException(message: String = "유효하지 않은 토큰입니다") : JwtException(message)

class ExpiredTokenException(message: String = "만료된 토큰입니다") : JwtException(message)

class TokenNotFoundException(message: String = "토큰을 찾을 수 없습니다") : JwtException(message)

class AuthenticationException(message: String = "인증에 실패했습니다") : JwtException(message)
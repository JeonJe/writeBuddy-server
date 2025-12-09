package com.writebuddy.writebuddy.security

import com.writebuddy.writebuddy.config.JwtProperties
import com.writebuddy.writebuddy.domain.UserRole
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    val jwtProperties: JwtProperties
) {
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    /**
     * Access Token 생성
     */
    fun generateAccessToken(userId: Long, email: String, roles: Set<UserRole>): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.accessTokenValidity)

        return Jwts.builder()
            .subject(userId.toString())
            .claim(CLAIM_EMAIL, email)
            .claim(CLAIM_ROLES, roles.map { it.name })
            .issuer(jwtProperties.issuer)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    /**
     * Refresh Token 생성
     */
    fun generateRefreshToken(userId: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.refreshTokenValidity)

        return Jwts.builder()
            .subject(userId.toString())
            .issuer(jwtProperties.issuer)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    /**
     * 토큰 검증
     */
    fun validateToken(token: String): Boolean {
        return try {
            if (token.isBlank()) {
                return false
            }
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            logger.warn("JWT 토큰 검증 실패: {}", e.message)
            false
        }
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    fun getUserIdFromToken(token: String): Long {
        val claims = getClaims(token)
        return claims.subject.toLong()
    }

    /**
     * 토큰에서 이메일 추출
     */
    fun getEmailFromToken(token: String): String {
        val claims = getClaims(token)
        return claims[CLAIM_EMAIL, String::class.java]
    }

    /**
     * 토큰에서 역할 추출
     */
    fun getRolesFromToken(token: String): Set<UserRole> {
        val claims = getClaims(token)
        @Suppress("UNCHECKED_CAST")
        val roleNames = claims[CLAIM_ROLES, List::class.java] as List<String>
        return roleNames.map { UserRole.valueOf(it) }.toSet()
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    companion object {
        private const val CLAIM_EMAIL = "email"
        private const val CLAIM_ROLES = "roles"
    }
}
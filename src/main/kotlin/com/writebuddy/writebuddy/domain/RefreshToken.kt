package com.writebuddy.writebuddy.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "refresh_tokens",
    indexes = [
        Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
        Index(name = "idx_refresh_token_expiry", columnList = "expiry_date")
    ]
)
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false, length = 512)
    val token: String,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "expiry_date", nullable = false)
    val expiryDate: LocalDateTime,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 토큰이 만료되었는지 확인
     */
    fun isExpired(): Boolean {
        return !LocalDateTime.now().isBefore(expiryDate)
    }
}
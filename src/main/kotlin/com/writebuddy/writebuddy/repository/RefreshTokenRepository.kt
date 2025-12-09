package com.writebuddy.writebuddy.repository

import com.writebuddy.writebuddy.domain.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 문자열로 RefreshToken 조회
     */
    fun findByToken(token: String): RefreshToken?

    /**
     * 사용자 ID로 RefreshToken 조회
     */
    fun findByUserId(userId: Long): RefreshToken?

    /**
     * 사용자 ID로 RefreshToken 삭제 (로그아웃 시 사용)
     */
    fun deleteByUserId(userId: Long)
}
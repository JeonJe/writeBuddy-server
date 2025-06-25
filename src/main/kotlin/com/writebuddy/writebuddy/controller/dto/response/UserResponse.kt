package com.writebuddy.writebuddy.controller.dto.response

import com.writebuddy.writebuddy.domain.User
import java.time.LocalDateTime

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                username = user.username,
                email = user.email,
                createdAt = user.createdAt ?: LocalDateTime.now()
            )
        }
    }
}
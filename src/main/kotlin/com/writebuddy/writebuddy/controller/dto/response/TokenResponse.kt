package com.writebuddy.writebuddy.controller.dto.response

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)
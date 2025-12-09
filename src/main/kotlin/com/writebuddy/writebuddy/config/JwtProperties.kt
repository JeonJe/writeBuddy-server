package com.writebuddy.writebuddy.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenValidity: Long,
    val refreshTokenValidity: Long,
    val issuer: String
)
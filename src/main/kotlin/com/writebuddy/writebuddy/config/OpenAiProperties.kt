package com.writebuddy.writebuddy.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "openai")
data class OpenAiProperties @ConstructorBinding constructor(
    val api: Api,
    val retry: Retry
) {
    data class Api(
        val key: String,
        val baseUrl: String,
        val model: String,
        val temperature: Double,
        val endpoint: String
    )
    
    data class Retry(
        val maxAttempts: Int,
        val delay: Long,
        val multiplier: Double
    )
}
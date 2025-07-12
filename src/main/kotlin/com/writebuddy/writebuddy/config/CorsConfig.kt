package com.writebuddy.writebuddy.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig {

    @Value("\${cors.allowed-origins:http://localhost:3000,http://localhost:7070}")
    private lateinit var allowedOrigins: String

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        
        // 환경별 허용 Origin 설정
        val origins = allowedOrigins.split(",").map { it.trim() }.toMutableList()
        origins.addAll(listOf(
            "http://localhost:*",     // 개발용 모든 로컬호스트 포트
            "http://127.0.0.1:*",     // 개발용 모든 127.0.0.1 포트
            "https://accounts.google.com"  // Google OAuth
        ))
        
        configuration.allowedOriginPatterns = origins
        
        // 허용할 HTTP 메서드
        configuration.allowedMethods = listOf(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        )
        
        // 허용할 헤더
        configuration.allowedHeaders = listOf("*")
        
        // 인증 정보 포함 허용
        configuration.allowCredentials = true
        
        // Preflight 요청 캐시 시간 (초)
        configuration.maxAge = 3600L
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        
        return source
    }
}

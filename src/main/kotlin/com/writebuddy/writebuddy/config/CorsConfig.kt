package com.writebuddy.writebuddy.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig {

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        
        // 허용할 Origin 설정 (웹 서버용)
        configuration.allowedOriginPatterns = listOf(
            "http://localhost:8080",  // 웹 서버
            "http://localhost:9090",  // 추가 웹 서버
            "http://localhost:9091",  // 백엔드 서버
            "http://127.0.0.1:8080",
            "http://127.0.0.1:9090", 
            "http://127.0.0.1:9091"
        )
        
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
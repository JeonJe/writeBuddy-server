package com.writebuddy.writebuddy.config

import com.writebuddy.writebuddy.security.JwtAuthenticationFilter
import com.writebuddy.writebuddy.security.JwtTokenProvider
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@TestConfiguration
@EnableWebSecurity
class SecurityTestConfig {

    @Bean
    @Primary
    fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { requests ->
                requests.anyRequest().permitAll()
            }
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun jwtTokenProvider(): JwtTokenProvider {
        return mock(JwtTokenProvider::class.java)
    }

    @Bean
    fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
        return mock(JwtAuthenticationFilter::class.java)
    }
}
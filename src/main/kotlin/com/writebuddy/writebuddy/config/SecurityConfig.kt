package com.writebuddy.writebuddy.config

import com.writebuddy.writebuddy.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val corsConfig: CorsConfig,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfig.corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { requests ->
                requests
                    // Public endpoints
                    .requestMatchers("/auth/**", "/users/register", "/error", "/h2-console/**").permitAll()
                    .requestMatchers("/health/**").permitAll()

                    // Temporarily permit all for development
                    .requestMatchers("/corrections/**", "/examples/**", "/chat", "/statistics").permitAll()

                    // User endpoints (JWT required)
                    .requestMatchers("/corrections/users/**", "/users/**", "/analytics/**").hasRole("USER")

                    // Admin endpoints
                    .requestMatchers("/admin/**").hasRole("ADMIN")

                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )
            .headers { headers ->
                headers.frameOptions { it.disable() }
            }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

}

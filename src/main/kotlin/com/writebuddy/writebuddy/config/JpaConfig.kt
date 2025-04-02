package com.writebuddy.writebuddy.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.*

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
class JpaConfig {
    @Bean
    fun auditorProvider(): AuditorAware<String> {
        // 임시 하드코딩 (나중에 Security 연동 가능)
        return AuditorAware { Optional.of("system") }
    }
}

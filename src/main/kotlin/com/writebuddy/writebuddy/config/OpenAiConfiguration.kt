package com.writebuddy.writebuddy.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenAiProperties::class)
class OpenAiConfiguration
package com.writebuddy.writebuddy.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

@Configuration
class OpenApiRestClientConfig {

    @Value("\${openai.api.key}")
    private lateinit var apiKey: String
    
    @Value("\${openai.api.base-url}")
    private lateinit var baseUrl: String

    @Bean
    fun restClient(): RestClient {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeaders { headers ->
                headers.contentType = MediaType.APPLICATION_JSON
                headers.setBearerAuth(apiKey)
            }
            .build()
    }
}

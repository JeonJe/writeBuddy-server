package com.writebuddy.writebuddy.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class OpenApiRestClientConfig {

    @Value("\${openai.api.key}")
    private lateinit var apiKey: String
    
    @Value("\${openai.api.base-url}")
    private lateinit var baseUrl: String
    
    @Value("\${openai.timeout.connect:10}")
    private var connectTimeout: Int = 10
    
    @Value("\${openai.timeout.read:30}")
    private var readTimeout: Int = 30

    @Bean
    fun restClient(): RestClient {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(createRequestFactory())
            .defaultHeaders { headers ->
                headers.contentType = MediaType.APPLICATION_JSON
                headers.setBearerAuth(apiKey)
            }
            .build()
    }
    
    private fun createRequestFactory(): ClientHttpRequestFactory {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(Duration.ofSeconds(connectTimeout.toLong()))
        factory.setReadTimeout(Duration.ofSeconds(readTimeout.toLong()))
        return factory
    }
}

package com.writebuddy.writebuddy.config

import com.writebuddy.writebuddy.service.OpenAiClient
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestClient

@TestConfiguration
class TestConfiguration {

    @Bean
    @Primary
    fun mockRestClient(): RestClient {
        return Mockito.mock(RestClient::class.java)
    }

    @Bean
    @Primary
    fun mockOpenAiClient(): OpenAiClient {
        val mockClient = Mockito.mock(OpenAiClient::class.java)
        
        // Default mock behavior
        Mockito.`when`(mockClient.generateCorrectionAndFeedbackWithType(Mockito.anyString()))
            .thenReturn(Triple("Corrected sentence", "Mock feedback", "GRAMMAR"))
            
        return mockClient
    }
}
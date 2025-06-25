package com.writebuddy.writebuddy.config

import com.writebuddy.writebuddy.service.OpenAiClient
import com.writebuddy.writebuddy.service.Quadruple
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestClient

@TestConfiguration
class TestConfiguration {

    @Bean
    @Primary
    fun testRestClient(): RestClient {
        return Mockito.mock(RestClient::class.java)
    }

    @Bean
    @Primary
    fun mockOpenAiClient(): OpenAiClient {
        val mockClient = Mockito.mock(OpenAiClient::class.java)
        
        // Default mock behavior for new method with score
        Mockito.`when`(mockClient.generateCorrectionAndFeedbackWithScore(Mockito.anyString()))
            .thenReturn(Quadruple("Corrected sentence", "Mock feedback", "GRAMMAR", 8))
            
        // Legacy method for backward compatibility
        Mockito.`when`(mockClient.generateCorrectionAndFeedbackWithType(Mockito.anyString()))
            .thenReturn(Triple("Corrected sentence", "Mock feedback", "GRAMMAR"))
            
        return mockClient
    }
}
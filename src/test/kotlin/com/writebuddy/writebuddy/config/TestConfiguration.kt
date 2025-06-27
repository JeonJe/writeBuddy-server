package com.writebuddy.writebuddy.config

import com.writebuddy.writebuddy.service.*
import com.writebuddy.writebuddy.config.OpenAiProperties
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
        
        // Default mock behavior for new method with translations
        Mockito.`when`(mockClient.generateCorrectionWithTranslations(Mockito.anyString()))
            .thenReturn(Sextuple("Corrected sentence", "Mock feedback", "GRAMMAR", 8, "원문 번역", "교정문 번역"))
            
        return mockClient
    }
    
    @Bean
    @Primary
    fun mockRealExampleService(): RealExampleService {
        val mockService = Mockito.mock(RealExampleService::class.java)
        
        // Default mock behavior - return empty list
        Mockito.`when`(mockService.findRelatedExamples(Mockito.anyString()))
            .thenReturn(emptyList())
            
        return mockService
    }
    
    @Bean
    @Primary
    fun mockPromptManager(): PromptManager {
        val mockService = Mockito.mock(PromptManager::class.java)
        
        // Default mock behaviors
        Mockito.`when`(mockService.getCorrectionSystemPrompt())
            .thenReturn("Mock system prompt")
        Mockito.`when`(mockService.getCorrectionUserPrompt(Mockito.anyString()))
            .thenReturn("Mock user prompt")
        Mockito.`when`(mockService.getCorrectionFallback())
            .thenReturn("Mock fallback")
            
        return mockService
    }
    
    @Bean
    @Primary
    fun mockOpenAiResponseParser(): OpenAiResponseParser {
        val mockService = Mockito.mock(OpenAiResponseParser::class.java)
        
        // Default mock behavior
        Mockito.`when`(mockService.parseResponseWithTranslations(Mockito.anyString()))
            .thenReturn(Sextuple("Corrected sentence", "Mock feedback", "GRAMMAR", 8, "원문 번역", "교정문 번역"))
            
        return mockService
    }
    
    @Bean
    @Primary
    fun mockOpenAiProperties(): OpenAiProperties {
        val mockProperties = Mockito.mock(OpenAiProperties::class.java)
        val mockApi = Mockito.mock(OpenAiProperties.Api::class.java)
        val mockRetry = Mockito.mock(OpenAiProperties.Retry::class.java)
        
        Mockito.`when`(mockApi.key).thenReturn("test-key")
        Mockito.`when`(mockApi.model).thenReturn("gpt-4o-mini")
        Mockito.`when`(mockApi.temperature).thenReturn(0.3)
        Mockito.`when`(mockApi.endpoint).thenReturn("/chat/completions")
        Mockito.`when`(mockApi.baseUrl).thenReturn("https://api.openai.com/v1")
        
        Mockito.`when`(mockRetry.maxAttempts).thenReturn(3)
        Mockito.`when`(mockRetry.delay).thenReturn(1000L)
        Mockito.`when`(mockRetry.multiplier).thenReturn(2.0)
        
        Mockito.`when`(mockProperties.api).thenReturn(mockApi)
        Mockito.`when`(mockProperties.retry).thenReturn(mockRetry)
        
        return mockProperties
    }
}
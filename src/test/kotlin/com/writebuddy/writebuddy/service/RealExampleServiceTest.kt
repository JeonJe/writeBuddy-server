package com.writebuddy.writebuddy.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.writebuddy.writebuddy.domain.ExampleSourceType
import com.writebuddy.writebuddy.domain.RealExample
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.Mock
import org.junit.jupiter.api.BeforeEach
import org.mockito.MockitoAnnotations

@DisplayName("RealExampleService 테스트")
class RealExampleServiceTest {

    @Mock
    private lateinit var openAiClient: OpenAiClient
    
    @Mock
    private lateinit var promptManager: PromptManager
    
    private lateinit var objectMapper: ObjectMapper
    private lateinit var realExampleService: RealExampleService
    
    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        objectMapper = ObjectMapper()
        realExampleService = RealExampleService(openAiClient, promptManager, objectMapper)
    }

    @Nested
    @DisplayName("AI 예시 생성 기능")
    inner class AIExampleGenerationTest {
        
        @Test
        @DisplayName("OpenAI로 관련 예시를 생성한다")
        fun generatesExamplesWithOpenAI() {
            // given
            val correctedSentence = "I want to learn English well"
            val systemPrompt = "test system prompt"
            val userPrompt = "test user prompt"
            val mockResponse = """
            {
              "examples": [
                {
                  "phrase": "I want to learn English well",
                  "source": "Test Movie",
                  "sourceType": "MOVIE",
                  "context": "Test context",
                  "difficulty": 5,
                  "tags": ["test", "learning"],
                  "isVerified": true
                }
              ]
            }
            """.trimIndent()
            
            given(promptManager.getExampleGenerationSystemPrompt()).willReturn(systemPrompt)
            given(promptManager.getExampleGenerationUserPrompt(correctedSentence)).willReturn(userPrompt)
            given(openAiClient.sendChatRequest(systemPrompt, userPrompt)).willReturn(mockResponse)

            // when
            val result = realExampleService.findRelatedExamples(correctedSentence)

            // then
            assertThat(result).hasSize(1)
            assertThat(result.first().phrase).isEqualTo("I want to learn English well")
            assertThat(result.first().source).isEqualTo("Test Movie")
            assertThat(result.first().sourceType).isEqualTo(ExampleSourceType.MOVIE)
        }
        
        @Test
        @DisplayName("OpenAI 호출 실패 시 빈 목록을 반환한다")
        fun returnsEmptyListWhenOpenAIFails() {
            // given
            val correctedSentence = "Test sentence"
            given(promptManager.getExampleGenerationSystemPrompt()).willReturn("system")
            given(promptManager.getExampleGenerationUserPrompt(correctedSentence)).willReturn("user")
            given(openAiClient.sendChatRequest(any(), any())).willThrow(RuntimeException("API Error"))

            // when
            val result = realExampleService.findRelatedExamples(correctedSentence)

            // then
            assertThat(result).isEmpty()
        }
    }


    private fun createMockExample(
        phrase: String, 
        sourceType: ExampleSourceType, 
        difficulty: Int = 5
    ): RealExample {
        return RealExample(
            phrase = phrase,
            source = "Test Source",
            sourceType = sourceType,
            context = "Test context",
            difficulty = difficulty,
            tags = "test,example",
            isVerified = true
        )
    }
}
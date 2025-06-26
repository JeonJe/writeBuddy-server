package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.domain.ExampleSourceType
import com.writebuddy.writebuddy.domain.RealExample
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
@DisplayName("RealExampleService 테스트")
class RealExampleServiceTest {

    
    @MockBean
    private lateinit var openAiClient: OpenAiClient
    
    @MockBean
    private lateinit var promptManager: PromptManager

    @Autowired
    private lateinit var realExampleService: RealExampleService

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
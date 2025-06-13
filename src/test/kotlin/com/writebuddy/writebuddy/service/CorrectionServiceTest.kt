package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.controller.dto.request.CorrectionRequest
import com.writebuddy.writebuddy.domain.Correction
import com.writebuddy.writebuddy.repository.CorrectionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@DisplayName("CorrectionService 테스트")
class CorrectionServiceTest {

    @Autowired
    private lateinit var correctionService: CorrectionService

    @MockitoBean
    private lateinit var correctionRepository: CorrectionRepository

    @MockitoBean
    private lateinit var openAiClient: OpenAiClient

    @Nested
    @DisplayName("교정 저장 기능")
    inner class SaveCorrectionTests {
        
        @Test
        @DisplayName("OpenAI 응답을 받아 교정 결과를 저장한다")
        fun save_successWithOpenAiResponse() {
            val request = CorrectionRequest("hello world")
            val mockCorrection = Correction(1L, "hello world", "Hello, world!", "대문자로 시작해야 합니다.")
            
            given(openAiClient.generateCorrectionAndFeedback("hello world"))
                .willReturn("Hello, world!" to "대문자로 시작해야 합니다.")
            given(correctionRepository.save(any()))
                .willReturn(mockCorrection)

            val result = correctionService.save(request)

            assertThat(result)
                .extracting("originSentence", "correctedSentence", "feedback")
                .containsExactly("hello world", "Hello, world!", "대문자로 시작해야 합니다.")
        }
    }

    @Nested
    @DisplayName("교정 목록 조회 기능")
    inner class GetAllCorrectionsTests {
        
        @Test
        @DisplayName("모든 교정 결과를 반환한다")
        fun getAll_returnAllCorrections() {
            val corrections = listOf(
                Correction(1L, "hello world", "Hello, world!", "대문자로 시작해야 합니다."),
                Correction(2L, "i am student", "I am a student.", "대문자와 관사를 추가해야 합니다.")
            )
            
            given(correctionRepository.findAll()).willReturn(corrections)

            val result = correctionService.getAll()

            assertThat(result)
                .hasSize(2)
                .extracting("originSentence")
                .containsExactly("hello world", "i am student")
        }
    }
}

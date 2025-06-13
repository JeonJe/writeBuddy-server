package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.controller.dto.request.CorrectionRequest
import com.writebuddy.writebuddy.domain.Correction
import com.writebuddy.writebuddy.domain.ErrorType
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
            val mockCorrection = Correction(
                id = 1L,
                originSentence = "hello world",
                correctedSentence = "Hello, world!",
                feedback = "대문자로 시작해야 합니다.",
                errorType = ErrorType.GRAMMAR
            )
            
            given(openAiClient.generateCorrectionAndFeedbackWithType("hello world"))
                .willReturn(Triple("Hello, world!", "대문자로 시작해야 합니다.", "GRAMMAR"))
            given(correctionRepository.save(any()))
                .willReturn(mockCorrection)

            val result = correctionService.save(request)

            assertThat(result)
                .extracting("originSentence", "correctedSentence", "feedback", "errorType")
                .containsExactly("hello world", "Hello, world!", "대문자로 시작해야 합니다.", ErrorType.GRAMMAR)
        }
    }

    @Nested
    @DisplayName("교정 목록 조회 기능")
    inner class GetAllCorrectionsTests {
        
        @Test
        @DisplayName("모든 교정 결과를 반환한다")
        fun getAll_returnAllCorrections() {
            val corrections = listOf(
                Correction(
                    id = 1L,
                    originSentence = "hello world",
                    correctedSentence = "Hello, world!",
                    feedback = "대문자로 시작해야 합니다.",
                    errorType = ErrorType.GRAMMAR
                ),
                Correction(
                    id = 2L,
                    originSentence = "i am student",
                    correctedSentence = "I am a student.",
                    feedback = "대문자와 관사를 추가해야 합니다.",
                    errorType = ErrorType.GRAMMAR
                )
            )
            
            given(correctionRepository.findAll()).willReturn(corrections)

            val result = correctionService.getAll()

            assertThat(result)
                .hasSize(2)
                .extracting("originSentence")
                .containsExactly("hello world", "i am student")
        }
    }

    @Nested
    @DisplayName("오류 타입 통계 조회 기능")
    inner class GetErrorTypeStatisticsTests {
        
        @Test
        @DisplayName("오류 타입별 통계를 반환한다")
        fun getErrorTypeStatistics_returnStatistics() {
            val corrections = listOf(
                Correction(
                    id = 1L,
                    originSentence = "hello world",
                    correctedSentence = "Hello, world!",
                    feedback = "대문자로 시작해야 합니다.",
                    errorType = ErrorType.GRAMMAR
                ),
                Correction(
                    id = 2L,
                    originSentence = "speling mistake",
                    correctedSentence = "spelling mistake",
                    feedback = "철자를 확인하세요.",
                    errorType = ErrorType.SPELLING
                ),
                Correction(
                    id = 3L,
                    originSentence = "another grammar",
                    correctedSentence = "Another grammar",
                    feedback = "대문자로 시작하세요.",
                    errorType = ErrorType.GRAMMAR
                )
            )
            
            given(correctionRepository.findAll()).willReturn(corrections)

            val result = correctionService.getErrorTypeStatistics()

            assertThat(result)
                .hasSize(2)
                .containsEntry(ErrorType.GRAMMAR, 2L)
                .containsEntry(ErrorType.SPELLING, 1L)
        }
    }

    @Nested
    @DisplayName("오류 타입 파싱 기능")
    inner class ParseErrorTypeTests {
        
        @Test
        @DisplayName("유효한 오류 타입 문자열을 파싱한다")
        fun parseErrorType_validErrorType() {
            val request = CorrectionRequest("test sentence")
            val mockCorrection = Correction(
                id = 1L,
                originSentence = "test sentence",
                correctedSentence = "Test sentence.",
                feedback = "테스트 피드백",
                errorType = ErrorType.SPELLING
            )
            
            given(openAiClient.generateCorrectionAndFeedbackWithType("test sentence"))
                .willReturn(Triple("Test sentence.", "테스트 피드백", "spelling"))
            given(correctionRepository.save(any()))
                .willReturn(mockCorrection)

            val result = correctionService.save(request)

            assertThat(result.errorType).isEqualTo(ErrorType.SPELLING)
        }
        
        @Test
        @DisplayName("잘못된 오류 타입 문자열은 SYSTEM으로 기본값 설정")
        fun parseErrorType_invalidErrorTypeFallbackToSystem() {
            val request = CorrectionRequest("test sentence")
            val mockCorrection = Correction(
                id = 1L,
                originSentence = "test sentence",
                correctedSentence = "Test sentence.",
                feedback = "테스트 피드백",
                errorType = ErrorType.SYSTEM
            )
            
            given(openAiClient.generateCorrectionAndFeedbackWithType("test sentence"))
                .willReturn(Triple("Test sentence.", "테스트 피드백", "invalid_type"))
            given(correctionRepository.save(any()))
                .willReturn(mockCorrection)

            val result = correctionService.save(request)

            assertThat(result.errorType).isEqualTo(ErrorType.SYSTEM)
        }
    }
}
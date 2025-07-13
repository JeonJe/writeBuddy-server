package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.controller.dto.request.CorrectionRequest
import com.writebuddy.writebuddy.domain.Correction
import com.writebuddy.writebuddy.domain.FeedbackType
import com.writebuddy.writebuddy.repository.CorrectionRepository
import com.writebuddy.writebuddy.repository.UserRepository
import com.writebuddy.writebuddy.service.Sextuple
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
    
    @MockitoBean
    private lateinit var userRepository: UserRepository

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
                feedbackType = FeedbackType.GRAMMAR,
                score = 8
            )
            
            given(openAiClient.generateCorrectionWithTranslations("hello world"))
                .willReturn(Sextuple("Hello, world!", "대문자로 시작해야 합니다.", "GRAMMAR", 8, "안녕 세상", "안녕, 세상!"))
            given(correctionRepository.save(any()))
                .willReturn(mockCorrection)

            val result = correctionService.save(request)

            assertThat(result)
                .extracting("originSentence", "correctedSentence", "feedback", "feedbackType")
                .containsExactly("hello world", "Hello, world!", "대문자로 시작해야 합니다.", FeedbackType.GRAMMAR)
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
                    feedbackType = FeedbackType.GRAMMAR
                ),
                Correction(
                    id = 2L,
                    originSentence = "i am student",
                    correctedSentence = "I am a student.",
                    feedback = "대문자와 관사를 추가해야 합니다.",
                    feedbackType = FeedbackType.GRAMMAR
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
    @DisplayName("피드백 타입 통계 조회 기능")
    inner class GetFeedbackTypeStatisticsTests {
        
        @Test
        @DisplayName("피드백 타입별 통계를 반환한다")
        fun getFeedbackTypeStatistics_returnStatistics() {
            val corrections = listOf(
                Correction(
                    id = 1L,
                    originSentence = "hello world",
                    correctedSentence = "Hello, world!",
                    feedback = "대문자로 시작해야 합니다.",
                    feedbackType = FeedbackType.GRAMMAR
                ),
                Correction(
                    id = 2L,
                    originSentence = "speling mistake",
                    correctedSentence = "spelling mistake",
                    feedback = "철자를 확인하세요.",
                    feedbackType = FeedbackType.SPELLING
                ),
                Correction(
                    id = 3L,
                    originSentence = "another grammar",
                    correctedSentence = "Another grammar",
                    feedback = "대문자로 시작하세요.",
                    feedbackType = FeedbackType.GRAMMAR
                )
            )
            
            given(correctionRepository.findAll()).willReturn(corrections)

            val result = correctionService.getFeedbackTypeStatistics()

            assertThat(result)
                .hasSize(2)
                .containsEntry("GRAMMAR", 2)
                .containsEntry("SPELLING", 1)
        }
    }

    @Nested
    @DisplayName("피드백 타입 파싱 기능")
    inner class ParseFeedbackTypeTests {
        
        @Test
        @DisplayName("유효한 피드백 타입 문자열을 파싱한다")
        fun parseFeedbackType_validFeedbackType() {
            val request = CorrectionRequest("test sentence")
            val mockCorrection = Correction(
                id = 1L,
                originSentence = "test sentence",
                correctedSentence = "Test sentence.",
                feedback = "테스트 피드백",
                feedbackType = FeedbackType.SPELLING,
                score = 7
            )
            
            given(openAiClient.generateCorrectionWithTranslations("test sentence"))
                .willReturn(Sextuple("Test sentence.", "테스트 피드백", "spelling", 7, "테스트 문장", "테스트 문장."))
            given(correctionRepository.save(any()))
                .willReturn(mockCorrection)

            val result = correctionService.save(request)

            assertThat(result.feedbackType).isEqualTo(FeedbackType.SPELLING)
        }
        
        @Test
        @DisplayName("잘못된 피드백 타입 문자열은 SYSTEM으로 기본값 설정")
        fun parseFeedbackType_invalidFeedbackTypeFallbackToSystem() {
            val request = CorrectionRequest("test sentence")
            val mockCorrection = Correction(
                id = 1L,
                originSentence = "test sentence",
                correctedSentence = "Test sentence.",
                feedback = "테스트 피드백",
                feedbackType = FeedbackType.SYSTEM,
                score = 5
            )
            
            given(openAiClient.generateCorrectionWithTranslations("test sentence"))
                .willReturn(Sextuple("Test sentence.", "테스트 피드백", "invalid_type", 5, "테스트 문장", "테스트 문장."))
            given(correctionRepository.save(any()))
                .willReturn(mockCorrection)

            val result = correctionService.save(request)

            assertThat(result.feedbackType).isEqualTo(FeedbackType.SYSTEM)
        }
    }
}
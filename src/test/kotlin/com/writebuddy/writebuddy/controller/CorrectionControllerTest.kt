package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.request.CorrectionRequest
import com.writebuddy.writebuddy.domain.Correction
import com.writebuddy.writebuddy.domain.FeedbackType
import com.writebuddy.writebuddy.domain.RealExample
import com.writebuddy.writebuddy.repository.CorrectionLightProjection
import com.writebuddy.writebuddy.service.CorrectionService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import com.writebuddy.writebuddy.config.SecurityTestConfig
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(CorrectionController::class)
@Import(SecurityTestConfig::class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CorrectionController 테스트")
class CorrectionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var correctionService: CorrectionService

    @Nested
    @DisplayName("교정 저장 API")
    inner class SaveCorrectionTests {

        @Test
        @WithMockUser
        @DisplayName("유효한 문장을 전달하면 교정 결과를 저장하고 반환한다")
        fun save_successScenario() {
            val request = CorrectionRequest("hello world")
            val saved = Correction(1L, request.originSentence, "Hello, world!", "대문자로 시작해야 합니다.")
            val examples = emptyList<RealExample>()

            given(correctionService.saveWithExamplesAsync(request, null)).willReturn(Pair(saved, examples))

            val json = """
            {
                "originSentence": "hello world"
            }
        """.trimIndent()

            mockMvc.perform(
                post("/corrections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(csrf())
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.originSentence").value("hello world"))
                .andExpect(jsonPath("$.correctedSentence").value("Hello, world!"))
                .andExpect(jsonPath("$.feedback").value("대문자로 시작해야 합니다."))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.relatedExamples").isArray)
                .andExpect(jsonPath("$.relatedExamples").isEmpty)
        }
    }

    @Nested
    @DisplayName("교정 목록 조회 API")
    inner class GetAllCorrectionsTests {

        @Test
        @WithMockUser
        @DisplayName("모든 교정 결과를 목록으로 반환한다")
        fun getAll_successScenario() {
            val projection1 = mock(CorrectionLightProjection::class.java)
            given(projection1.getId()).willReturn(1L)
            given(projection1.getOriginSentence()).willReturn("hello world")
            given(projection1.getCorrectedSentence()).willReturn("Hello, world!")
            given(projection1.getFeedbackType()).willReturn(FeedbackType.GRAMMAR)
            given(projection1.getScore()).willReturn(7)
            given(projection1.getIsFavorite()).willReturn(false)
            given(projection1.getCreatedAt()).willReturn(LocalDateTime.of(2025, 12, 25, 10, 0))

            val projection2 = mock(CorrectionLightProjection::class.java)
            given(projection2.getId()).willReturn(2L)
            given(projection2.getOriginSentence()).willReturn("i am student")
            given(projection2.getCorrectedSentence()).willReturn("I am a student.")
            given(projection2.getFeedbackType()).willReturn(FeedbackType.GRAMMAR)
            given(projection2.getScore()).willReturn(8)
            given(projection2.getIsFavorite()).willReturn(false)
            given(projection2.getCreatedAt()).willReturn(LocalDateTime.of(2025, 12, 25, 11, 0))

            val projections = listOf(projection1, projection2)
            given(correctionService.getAllLightweight()).willReturn(projections)

            mockMvc.perform(get("/corrections"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].originSentence").value("hello world"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].originSentence").value("i am student"))
        }
    }
}

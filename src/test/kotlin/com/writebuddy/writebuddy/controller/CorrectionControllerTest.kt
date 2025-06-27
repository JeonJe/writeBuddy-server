package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.request.CorrectionRequest
import com.writebuddy.writebuddy.domain.Correction
import com.writebuddy.writebuddy.service.CorrectionService
import com.writebuddy.writebuddy.service.RealExampleService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(CorrectionController::class)
@DisplayName("CorrectionController 테스트")
class CorrectionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var correctionService: CorrectionService
    
    @MockitoBean
    private lateinit var realExampleService: RealExampleService

    @Nested
    @DisplayName("교정 저장 API")
    inner class SaveCorrectionTests {
        
        @Test
        @WithMockUser
        @DisplayName("유효한 문장을 전달하면 교정 결과를 저장하고 반환한다")
        fun save_successScenario() {
            val request = CorrectionRequest("hello world")
            val saved = Correction(1L, request.originSentence, "Hello, world!", "대문자로 시작해야 합니다.")

            given(correctionService.save(request)).willReturn(saved)
            given(realExampleService.findRelatedExamples("Hello, world!")).willReturn(emptyList())

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
            val corrections = listOf(
                Correction(1L, "hello world", "Hello, world!", "대문자로 시작해야 합니다."),
                Correction(2L, "i am student", "I am a student.", "대문자와 관사를 추가해야 합니다.")
            )

            given(correctionService.getAll()).willReturn(corrections)

            mockMvc.perform(get("/corrections"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].originSentence").value("hello world"))
                .andExpect(jsonPath("$[0].relatedExamples").isArray)
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].originSentence").value("i am student"))
                .andExpect(jsonPath("$[1].relatedExamples").isArray)
        }
    }
}

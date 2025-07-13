package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.response.UnifiedStatisticsResponse
import com.writebuddy.writebuddy.service.StatisticsService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(StatisticsController::class)
@DisplayName("StatisticsController 테스트")
class StatisticsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var statisticsService: StatisticsService

    @Nested
    @DisplayName("통합 통계 API")
    inner class UnifiedStatisticsTests {
        
        @Test
        @WithMockUser
        @DisplayName("통합 통계를 요청하면 완전한 통계 데이터를 반환한다")
        fun getUnifiedStatistics_successScenario() {
            // given
            val mockResponse = createMockUnifiedStatisticsResponse()
            given(statisticsService.getUnifiedStatistics()).willReturn(mockResponse)

            // when & then
            mockMvc.perform(get("/statistics/unified"))
                .andExpect(status().isOk)
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.userStatistics.totalCorrections").value(25))
                .andExpect(jsonPath("$.userStatistics.averageScore").value(7.8))
                .andExpect(jsonPath("$.dashboardData.dailyStatistics.totalCorrections").value(5))
                .andExpect(jsonPath("$.generatedAt").exists())
        }

    }

    private fun createMockUnifiedStatisticsResponse(): UnifiedStatisticsResponse {
        val userStatistics = UnifiedStatisticsResponse.UserStatistics(
            totalCorrections = 25,
            averageScore = 7.8,
            favoriteCount = 8,
            feedbackTypeDistribution = mapOf("GRAMMAR" to 15, "SPELLING" to 6, "STYLE" to 4)
        )

        val correctionStatistics = UnifiedStatisticsResponse.CorrectionStatistics(
            feedbackTypeStatistics = mapOf("GRAMMAR" to 15, "SPELLING" to 8),
            averageScore = 7.2
        )

        val dailyStatistics = UnifiedStatisticsResponse.DailyStatistics(
            totalCorrections = 5,
            averageScore = 8.1,
            feedbackTypes = mapOf("GRAMMAR" to 3, "SPELLING" to 1)
        )

        val scoreTrend = listOf(
            UnifiedStatisticsResponse.ScoreTrendItem(
                order = 1,
                score = 6,
                feedbackType = "GRAMMAR",
                createdAt = LocalDateTime.of(2025, 6, 25, 10, 30, 0)
            )
        )

        val errorPatterns = mapOf(
            "GRAMMAR" to listOf("i am student", "how can i"),
            "SPELLING" to listOf("recieve", "seperate")
        )

        val dashboardData = UnifiedStatisticsResponse.DashboardData(
            dailyStatistics = dailyStatistics,
            scoreTrend = scoreTrend,
            errorPatterns = errorPatterns
        )

        return UnifiedStatisticsResponse(
            userStatistics = userStatistics,
            correctionStatistics = correctionStatistics,
            dashboardData = dashboardData,
            generatedAt = LocalDateTime.of(2025, 7, 13, 14, 30, 0)
        )
    }
}
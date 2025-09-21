package com.writebuddy.writebuddy.controller.dto.response

import java.time.LocalDateTime

data class UnifiedStatisticsResponse(
    val correctionStatistics: CorrectionStatistics,
    val dashboardData: DashboardData,
    val generatedAt: LocalDateTime = LocalDateTime.now()
) {
    
    data class CorrectionStatistics(
        val feedbackTypeStatistics: Map<String, Int>,
        val averageScore: Double
    )
    
    data class DashboardData(
        val dailyStatistics: DailyStatistics,
        val scoreTrend: List<ScoreTrendItem>,
        val errorPatterns: Map<String, List<String>>
    )
    
    data class DailyStatistics(
        val totalCorrections: Int,
        val averageScore: Double,
        val feedbackTypes: Map<String, Int>
    )
    
    data class ScoreTrendItem(
        val order: Int,
        val score: Int,
        val feedbackType: String,
        val createdAt: LocalDateTime
    )
}
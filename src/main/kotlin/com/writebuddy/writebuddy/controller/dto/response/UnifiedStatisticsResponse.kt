package com.writebuddy.writebuddy.controller.dto.response

import java.time.LocalDateTime

data class UnifiedStatisticsResponse(
    // Correction Statistics
    val correctionStatistics: CorrectionStatistics,
    
    // Dashboard Data
    val dashboardData: DashboardData,
    
    val generatedAt: LocalDateTime = LocalDateTime.now()
) {
    // TODO: UserStatistics - 사용자 ID 기능 추가 시 구현 예정
    // data class UserStatistics(
    //     val totalCorrections: Int,
    //     val averageScore: Double,
    //     val favoriteCount: Int,
    //     val feedbackTypeDistribution: Map<String, Int>
    // )
    
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
package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.controller.dto.response.UnifiedStatisticsResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class StatisticsService(
    private val correctionService: CorrectionService
) {
    private val logger: Logger = LoggerFactory.getLogger(StatisticsService::class.java)
    
    fun getStatistics(): UnifiedStatisticsResponse {
        logger.info("통합 통계 조회 시작: 교정 데이터 기반")
        
        // 1. Correction Statistics
        val feedbackTypeStats = correctionService.getFeedbackTypeStatistics()
        val avgScore = correctionService.getAverageScore()
        val correctionStatistics = UnifiedStatisticsResponse.CorrectionStatistics(
            feedbackTypeStatistics = feedbackTypeStats,
            averageScore = avgScore
        )
        
        // 2. Dashboard Data
        val dailyStats = correctionService.getDailyStatistics()
        val dailyStatistics = UnifiedStatisticsResponse.DailyStatistics(
            totalCorrections = dailyStats["totalCorrections"] as Int,
            averageScore = dailyStats["averageScore"] as Double,
            feedbackTypes = dailyStats["feedbackTypes"] as Map<String, Int>
        )
        
        val scoreTrend = correctionService.getScoreTrend().map { item ->
            UnifiedStatisticsResponse.ScoreTrendItem(
                order = item["order"] as Int,
                score = item["score"] as Int,
                feedbackType = item["feedbackType"] as String,
                createdAt = item["createdAt"] as java.time.LocalDateTime
            )
        }
        
        val errorPatterns = correctionService.getErrorPatternAnalysis()
        
        val dashboardData = UnifiedStatisticsResponse.DashboardData(
            dailyStatistics = dailyStatistics,
            scoreTrend = scoreTrend,
            errorPatterns = errorPatterns
        )
        
        logger.info("통합 통계 조회 완료: 교정 데이터 기반")
        
        return UnifiedStatisticsResponse(
            correctionStatistics = correctionStatistics,
            dashboardData = dashboardData
        )
    }
}

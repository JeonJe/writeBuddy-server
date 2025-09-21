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
        val totalStartTime = System.currentTimeMillis()
        logger.info("통합 통계 조회 시작: 교정 데이터 기반")
        
        val correctionStatsStart = System.currentTimeMillis()
        val feedbackTypeStats = correctionService.getFeedbackTypeStatistics()
        val avgScore = correctionService.getAverageScore()
        val correctionStatistics = UnifiedStatisticsResponse.CorrectionStatistics(
            feedbackTypeStatistics = feedbackTypeStats,
            averageScore = avgScore
        )
        val correctionStatsDuration = System.currentTimeMillis() - correctionStatsStart
        logger.info("교정 통계 섹션 완료: {}ms", correctionStatsDuration)
        
        val dashboardStart = System.currentTimeMillis()
        val dailyStats = correctionService.getDailyStatistics()
        val dailyStatistics = UnifiedStatisticsResponse.DailyStatistics(
            totalCorrections = dailyStats["totalCorrections"] as Int,
            averageScore = dailyStats["averageScore"] as Double,
            feedbackTypes = (dailyStats["feedbackTypes"] as? Map<*, *>)
                ?.mapKeys { it.key.toString() }
                ?.mapValues { it.value as Int }
                ?: emptyMap()
        )
        
        val scoreTrendStart = System.currentTimeMillis()
        val scoreTrend = correctionService.getScoreTrend().map { item ->
            UnifiedStatisticsResponse.ScoreTrendItem(
                order = item["order"] as Int,
                score = item["score"] as Int,
                feedbackType = item["feedbackType"] as String,
                createdAt = item["createdAt"] as java.time.LocalDateTime
            )
        }
        val scoreTrendDuration = System.currentTimeMillis() - scoreTrendStart
        logger.info("점수 트렌드 처리 완료: {}ms", scoreTrendDuration)
        
        val errorPatternsStart = System.currentTimeMillis()
        val errorPatterns = correctionService.getErrorPatternAnalysis()
        val errorPatternsDuration = System.currentTimeMillis() - errorPatternsStart
        logger.info("오류 패턴 분석 완료: {}ms", errorPatternsDuration)
        
        val dashboardData = UnifiedStatisticsResponse.DashboardData(
            dailyStatistics = dailyStatistics,
            scoreTrend = scoreTrend,
            errorPatterns = errorPatterns
        )
        val dashboardDuration = System.currentTimeMillis() - dashboardStart
        logger.info("대시보드 데이터 섹션 완료: {}ms", dashboardDuration)
        
        val totalDuration = System.currentTimeMillis() - totalStartTime
        logger.info("통합 통계 조회 전체 완료: {}ms (교정통계: {}ms, 대시보드: {}ms)", 
                   totalDuration, correctionStatsDuration, dashboardDuration)
        
        return UnifiedStatisticsResponse(
            correctionStatistics = correctionStatistics,
            dashboardData = dashboardData
        )
    }
}

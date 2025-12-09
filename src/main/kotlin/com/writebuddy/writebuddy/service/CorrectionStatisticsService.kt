package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.repository.CorrectionRepository
import com.writebuddy.writebuddy.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class CorrectionStatisticsService(
    private val correctionRepository: CorrectionRepository,
    private val userRepository: UserRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(CorrectionStatisticsService::class.java)

    fun getFeedbackTypeStatistics(): Map<String, Int> {
        val startTime = System.currentTimeMillis()
        val result = correctionRepository.getFeedbackTypeStatistics()
            .associate { it.getFeedbackType().name to it.getCount().toInt() }
        val duration = System.currentTimeMillis() - startTime
        logger.info("피드백 타입 통계 조회 완료: {}ms", duration)
        return result
    }

    fun getAverageScore(): Double {
        val startTime = System.currentTimeMillis()
        val result = correctionRepository.calculateOverallAverageScore() ?: 0.0
        val duration = System.currentTimeMillis() - startTime
        logger.info("평균 점수 계산 완료: {}ms", duration)
        return result
    }

    fun getDailyStatistics(): Map<String, Any> {
        val startTime = System.currentTimeMillis()
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay()

        val queryStart1 = System.currentTimeMillis()
        val dailyStats = correctionRepository.getDailyStatistics(startOfDay)
        val query1Duration = System.currentTimeMillis() - queryStart1
        logger.info("일별 통계 기본 쿼리 완료: {}ms", query1Duration)

        val queryStart2 = System.currentTimeMillis()
        val feedbackTypes = correctionRepository.getDailyFeedbackTypeCount(startOfDay)
            .associate { it.getFeedbackType().name to it.getCount().toInt() }
        val query2Duration = System.currentTimeMillis() - queryStart2
        logger.info("일별 피드백 타입 쿼리 완료: {}ms", query2Duration)

        val totalDuration = System.currentTimeMillis() - startTime
        logger.info("일별 통계 조회 전체 완료: {}ms", totalDuration)

        return mapOf(
            "totalCorrections" to (dailyStats?.getTotalCorrections()?.toInt() ?: 0),
            "averageScore" to (dailyStats?.getAverageScore() ?: 0.0),
            "feedbackTypes" to feedbackTypes
        )
    }

    fun getScoreTrend(limit: Int = 20): List<Map<String, Any>> {
        val startTime = System.currentTimeMillis()

        val queryStart = System.currentTimeMillis()
        val corrections = correctionRepository.findTop20ByOrderByCreatedAtDesc()
            .reversed()
        val queryDuration = System.currentTimeMillis() - queryStart
        logger.info("점수 변화 추이 쿼리 완료: {}ms, 조회된 건수: {}", queryDuration, corrections.size)

        val result = corrections.mapIndexed { index, correction ->
            mapOf<String, Any>(
                "order" to (index + 1),
                "score" to correction.getScore(),
                "feedbackType" to correction.getFeedbackType().name,
                "createdAt" to (correction.getCreatedAt() ?: java.time.LocalDateTime.now())
            )
        }

        val totalDuration = System.currentTimeMillis() - startTime
        logger.info("점수 변화 추이 조회 전체 완료: {}ms", totalDuration)
        return result
    }

    fun getErrorPatternAnalysis(): Map<String, List<String>> {
        val startTime = System.currentTimeMillis()

        val queryStart = System.currentTimeMillis()
        val errorPatterns = correctionRepository.findAllErrorPatterns()
        val queryDuration = System.currentTimeMillis() - queryStart
        logger.info("오류 패턴 쿼리 완료: {}ms, 조회된 건수: {}", queryDuration, errorPatterns.size)

        val processingStart = System.currentTimeMillis()
        val result = errorPatterns.groupBy { it.getFeedbackType() }
            .mapKeys { it.key.name }
            .mapValues { entry ->
                entry.value.map { it.getSentence() }
                    .distinct()
                    .take(3)
            }
        val processingDuration = System.currentTimeMillis() - processingStart
        logger.info("오류 패턴 데이터 처리 완료: {}ms", processingDuration)

        val totalDuration = System.currentTimeMillis() - startTime
        logger.info("오류 패턴 분석 전체 완료: {}ms", totalDuration)
        return result
    }

    fun getUserStatistics(userId: Long): Map<String, Any> {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }

        val corrections = user.corrections
        val scoresWithData = corrections.mapNotNull { it.score }

        return mapOf(
            "totalCorrections" to corrections.size,
            "averageScore" to if (scoresWithData.isNotEmpty()) scoresWithData.average() else 0.0,
            "favoriteCount" to corrections.count { it.isFavorite },
            "feedbackTypeDistribution" to corrections.groupBy { it.feedbackType.name }
                .mapValues { it.value.size }
        )
    }

    fun getAllUsersStatistics(): Map<String, Any> {
        val defaultUser = userRepository.findAll().firstOrNull()
            ?: throw IllegalStateException("기본 사용자가 존재하지 않습니다")
        return getUserStatistics(defaultUser.id)
    }
}

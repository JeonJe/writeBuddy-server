package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.controller.dto.request.CorrectionRequest
import com.writebuddy.writebuddy.domain.Correction
import com.writebuddy.writebuddy.domain.FeedbackType
import com.writebuddy.writebuddy.repository.CorrectionRepository
import com.writebuddy.writebuddy.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CorrectionService (
    private val correctionRepository: CorrectionRepository,
    private val openAiClient : OpenAiClient,
    private val userRepository: UserRepository,
){
    private val logger: Logger = LoggerFactory.getLogger(CorrectionService::class.java)
    fun save(request: CorrectionRequest): Correction {
        return save(request, null)
    }
    
    fun save(request: CorrectionRequest, userId: Long?): Correction {
        logger.info("교정 요청 처리 시작: {}, userId: {}", request.originSentence, userId)
        
        val (corrected, feedback, feedbackTypeStr, score) = openAiClient.generateCorrectionAndFeedbackWithScore(request.originSentence)
        val feedbackType = parseFeedbackType(feedbackTypeStr)

        val correction = Correction(
            originSentence = request.originSentence,
            correctedSentence = corrected,
            feedback = feedback,
            feedbackType = feedbackType,
            score = score
        )
        
        // Associate with user if userId is provided
        if (userId != null) {
            val user = userRepository.findById(userId)
                .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }
            correction.user = user
        }
        
        val savedCorrection = correctionRepository.save(correction)
        logger.info("교정 결과 저장 완료: id={}, feedbackType={}", savedCorrection.id, savedCorrection.feedbackType)
        
        return savedCorrection
    }

    fun getAll(): List<Correction> {
        logger.debug("전체 교정 목록 조회")
        return correctionRepository.findAll()
    }
    
    fun getFeedbackTypeStatistics(): Map<FeedbackType, Long> {
        logger.debug("피드백 타입 통계 조회")
        return correctionRepository.findAll()
            .groupBy { it.feedbackType }
            .mapValues { it.value.size.toLong() }
    }
    
    fun getAverageScore(): Double {
        logger.debug("평균 점수 계산")
        val corrections = correctionRepository.findAll()
        val scoresWithData = corrections.mapNotNull { it.score }
        
        return if (scoresWithData.isNotEmpty()) {
            scoresWithData.average()
        } else {
            0.0
        }
    }
    
    fun getDailyStatistics(): Map<String, Any> {
        logger.debug("일별 통계 조회")
        val corrections = correctionRepository.findAll()
        val today = java.time.LocalDate.now()
        val todayCorrections = corrections.filter { 
            it.createdAt?.toLocalDate() == today 
        }
        
        return mapOf(
            "totalCorrections" to todayCorrections.size,
            "averageScore" to if (todayCorrections.isNotEmpty()) {
                todayCorrections.mapNotNull { it.score }.average()
            } else 0.0,
            "feedbackTypes" to todayCorrections.groupBy { it.feedbackType }
                .mapValues { it.value.size }
        )
    }
    
    fun getScoreTrend(limit: Int = 20): List<Map<String, Any>> {
        logger.debug("점수 변화 추이 조회 (최근 {}개)", limit)
        val corrections = correctionRepository.findAll()
            .filter { it.score != null }
            .sortedByDescending { it.createdAt }
            .take(limit)
        
        return corrections.mapIndexed { index, correction ->
            mapOf<String, Any>(
                "order" to (corrections.size - index),
                "score" to correction.score!!,
                "feedbackType" to correction.feedbackType,
                "createdAt" to (correction.createdAt ?: java.time.LocalDateTime.now())
            )
        }.reversed()
    }
    
    fun getErrorPatternAnalysis(): Map<FeedbackType, List<String>> {
        logger.debug("오류 패턴 분석")
        val corrections = correctionRepository.findAll()
        
        return corrections.groupBy { it.feedbackType }
            .mapValues { (_, correctionList) ->
                correctionList.take(5).map { it.originSentence }
            }
    }
    
    fun toggleFavorite(id: Long): Correction {
        logger.debug("즐겨찾기 토글: id={}", id)
        val correction = correctionRepository.findById(id)
            .orElseThrow { IllegalArgumentException("교정 결과를 찾을 수 없습니다: $id") }
        
        correction.isFavorite = !correction.isFavorite
        return correctionRepository.save(correction)
    }
    
    fun getFavorites(): List<Correction> {
        logger.debug("즐겨찾기 목록 조회")
        return correctionRepository.findAll().filter { it.isFavorite }
    }
    
    fun updateMemo(id: Long, memo: String?): Correction {
        logger.debug("메모 업데이트: id={}, memo={}", id, memo)
        val correction = correctionRepository.findById(id)
            .orElseThrow { IllegalArgumentException("교정 결과를 찾을 수 없습니다: $id") }
        
        correction.memo = memo
        return correctionRepository.save(correction)
    }
    
    private fun parseFeedbackType(feedbackTypeStr: String): FeedbackType {
        return try {
            FeedbackType.valueOf(feedbackTypeStr.uppercase())
        } catch (e: IllegalArgumentException) {
            logger.warn("알 수 없는 피드백 타입: {}. SYSTEM으로 설정", feedbackTypeStr)
            FeedbackType.SYSTEM
        }
    }
}

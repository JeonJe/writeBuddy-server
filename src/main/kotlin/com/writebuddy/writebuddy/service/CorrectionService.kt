package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.controller.dto.request.CorrectionRequest
import com.writebuddy.writebuddy.domain.Correction
import com.writebuddy.writebuddy.domain.FeedbackType
import com.writebuddy.writebuddy.domain.RealExample
import com.writebuddy.writebuddy.domain.User
import com.writebuddy.writebuddy.repository.CorrectionRepository
import com.writebuddy.writebuddy.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class CorrectionService (
    private val correctionRepository: CorrectionRepository,
    private val openAiClient : OpenAiClient,
    private val userRepository: UserRepository,
    private val userService: UserService
){
    private val logger: Logger = LoggerFactory.getLogger(CorrectionService::class.java)
    fun save(request: CorrectionRequest): Correction {
        return save(request, null)
    }
    
    fun save(request: CorrectionRequest, userId: Long?): Correction {
        logger.info("교정 요청 처리 시작: {}", request.originSentence)
        
        val (corrected, feedback, feedbackTypeStr, score, originTranslation, correctedTranslation) = 
            openAiClient.generateCorrectionWithTranslations(request.originSentence)
        val feedbackType = parseFeedbackType(feedbackTypeStr)

        val correction = Correction(
            originSentence = request.originSentence,
            correctedSentence = corrected,
            feedback = feedback,
            feedbackType = feedbackType,
            score = score,
            originTranslation = originTranslation,
            correctedTranslation = correctedTranslation
        )
        
        // 항상 기본 사용자와 연결
        val defaultUser = userRepository.findAll().firstOrNull() ?: run {
            logger.info("기본 사용자 생성")
            userRepository.save(User(
                username = "demo_user", 
                email = "demo@writebuddy.com"
            ))
        }
        correction.user = defaultUser
        
        val savedCorrection = correctionRepository.save(correction)
        logger.info("교정 결과 저장 완료: id={}, feedbackType={}", savedCorrection.id, savedCorrection.feedbackType)
        
        return savedCorrection
    }
    
    // 통합 응답을 사용하여 교정과 예시를 함께 생성
    fun saveWithExamples(request: CorrectionRequest, userId: Long?): Pair<Correction, List<RealExample>> {
        logger.info("통합 교정 요청 처리 시작: {}", request.originSentence)
        
        val (correctionData, examples, success) = openAiClient.generateCorrectionWithExamples(request.originSentence)
        val (corrected, feedback, feedbackTypeStr, score, originTranslation, correctedTranslation) = correctionData
        
        logger.info("통합 응답 처리 성공: {}, 예시 개수: {}", success, examples.size)
        
        val feedbackType = parseFeedbackType(feedbackTypeStr)

        val correction = Correction(
            originSentence = request.originSentence,
            correctedSentence = corrected,
            feedback = feedback,
            feedbackType = feedbackType,
            score = score,
            originTranslation = originTranslation,
            correctedTranslation = correctedTranslation
        )
        
        // 항상 기본 사용자와 연결
        val defaultUser = userRepository.findAll().firstOrNull() ?: run {
            logger.info("기본 사용자 생성")
            userRepository.save(User(
                username = "demo_user", 
                email = "demo@writebuddy.com"
            ))
        }
        correction.user = defaultUser
        
        val savedCorrection = correctionRepository.save(correction)
        logger.info("교정 결과 저장 완료: id={}, feedbackType={}, 예시: {}개", 
                   savedCorrection.id, savedCorrection.feedbackType, examples.size)
        
        return Pair(savedCorrection, examples)
    }

    fun getAll(page: Int = 0, size: Int = 20): List<Correction> {
        val startTime = System.currentTimeMillis()
        logger.debug("교정 목록 조회 시작 - page: {}, size: {}", page, size)
        
        val result = correctionRepository.findAll(
            PageRequest.of(page, size)
        ).content
        
        val duration = System.currentTimeMillis() - startTime
        logger.info("교정 목록 조회 완료: {}ms, 조회된 건수: {}", duration, result.size)
        return result
    }
    
    fun getFeedbackTypeStatistics(): Map<String, Int> {
        val startTime = System.currentTimeMillis()
        logger.debug("피드백 타입 통계 조회 시작")
        val result = correctionRepository.getFeedbackTypeStatistics()
            .associate { it.getFeedbackType().name to it.getCount().toInt() }
        val duration = System.currentTimeMillis() - startTime
        logger.info("피드백 타입 통계 조회 완료: {}ms", duration)
        return result
    }
    
    fun getAverageScore(): Double {
        val startTime = System.currentTimeMillis()
        logger.debug("평균 점수 계산 시작")
        val result = correctionRepository.calculateOverallAverageScore() ?: 0.0
        val duration = System.currentTimeMillis() - startTime
        logger.info("평균 점수 계산 완료: {}ms", duration)
        return result
    }
    
    fun getDailyStatistics(): Map<String, Any> {
        val startTime = System.currentTimeMillis()
        logger.debug("일별 통계 조회 시작")
        val today = java.time.LocalDate.now()
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
        logger.debug("점수 변화 추이 조회 시작 (최근 {}개)", limit)
        
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
        logger.debug("오류 패턴 분석 시작")
        
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
                    .take(3)  // 각 타입별로 최대 3개의 예시만
            }
        val processingDuration = System.currentTimeMillis() - processingStart
        logger.info("오류 패턴 데이터 처리 완료: {}ms", processingDuration)
        
        val totalDuration = System.currentTimeMillis() - startTime
        logger.info("오류 패턴 분석 전체 완료: {}ms", totalDuration)
        return result
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
        return correctionRepository.findByIsFavoriteTrue()
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

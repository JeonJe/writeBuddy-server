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
import java.util.concurrent.TimeUnit

@Service
class CorrectionService (
    private val correctionRepository: CorrectionRepository,
    private val openAiClient : OpenAiClient,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val asyncCorrectionService: AsyncCorrectionService
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
        
        val defaultUser = userRepository.findAll().firstOrNull() ?: run {
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

    fun saveWithExamplesAsync(request: CorrectionRequest, userId: Long?): Pair<Correction, List<RealExample>> {
        val totalStartTime = System.currentTimeMillis()
        logger.info("비동기 병렬 교정 요청 시작: {}", request.originSentence)

        try {
            val future = asyncCorrectionService.generateCorrectionWithExamplesParallel(request.originSentence)
            val (correctionData, examples) = future[15, TimeUnit.SECONDS]
            val (corrected, feedback, feedbackTypeStr, score, originTranslation, correctedTranslation) = correctionData

            val processingTime = System.currentTimeMillis() - totalStartTime
            logger.info("비동기 처리 완료: {}ms (기존 동기 대비 {}ms 단축)",
                       processingTime, (20000 - processingTime).coerceAtLeast(0))

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

            val defaultUser = userRepository.findAll().firstOrNull() ?: run {
                logger.info("기본 사용자 생성")
                userRepository.save(User(
                    username = "demo_user",
                    email = "demo@writebuddy.com"
                ))
            }
            correction.user = defaultUser

            val savedCorrection = correctionRepository.save(correction)

            val totalTime = System.currentTimeMillis() - totalStartTime
            logger.info("전체 비동기 처리 완료: {}ms, id={}, 예시: {}개, 성능 개선: {}%",
                       totalTime, savedCorrection.id, examples.size,
                       ((20000.0 - totalTime) / 20000.0 * 100).coerceAtLeast(0.0).toInt())

            return Pair(savedCorrection, examples)

        } catch (e: java.util.concurrent.TimeoutException) {
            logger.error("비동기 처리 타임아웃 (15초), 동기 방식으로 폴백")
            return saveWithExamples(request, userId)
        } catch (e: Exception) {
            logger.error("비동기 처리 실패: {}, 동기 방식으로 폴백", e.message)
            return saveWithExamples(request, userId)
        }
    }

    fun getAll(page: Int = 0, size: Int = 20): List<Correction> {
        val startTime = System.currentTimeMillis()
        
        val result = correctionRepository.findAll(
            PageRequest.of(page, size)
        ).content
        
        val duration = System.currentTimeMillis() - startTime
        logger.info("교정 목록 조회 완료 (전체 데이터): {}ms, 조회된 건수: {}", duration, result.size)
        return result
    }
    
    fun getAllLightweight(page: Int = 0, size: Int = 20): List<com.writebuddy.writebuddy.repository.CorrectionLightProjection> {
        val startTime = System.currentTimeMillis()
        
        val result = correctionRepository.findAllLightweight(
            PageRequest.of(page, size)
        ).content
        
        val duration = System.currentTimeMillis() - startTime
        logger.info("교정 목록 조회 완료 (경량 데이터): {}ms, 조회된 건수: {}", duration, result.size)
        return result
    }
    
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
                    .take(3)  // 각 타입별로 최대 3개의 예시만
            }
        val processingDuration = System.currentTimeMillis() - processingStart
        logger.info("오류 패턴 데이터 처리 완료: {}ms", processingDuration)
        
        val totalDuration = System.currentTimeMillis() - startTime
        logger.info("오류 패턴 분석 전체 완료: {}ms", totalDuration)
        return result
    }
    
    fun toggleFavorite(id: Long): Correction {
        val correction = correctionRepository.findById(id)
            .orElseThrow { IllegalArgumentException("교정 결과를 찾을 수 없습니다: $id") }
        
        correction.isFavorite = !correction.isFavorite
        return correctionRepository.save(correction)
    }
    
    fun getFavorites(): List<Correction> {
        return correctionRepository.findByIsFavoriteTrue()
    }
    
    fun updateMemo(id: Long, memo: String?): Correction {
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

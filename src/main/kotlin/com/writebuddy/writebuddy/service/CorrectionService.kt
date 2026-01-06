package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.controller.dto.request.CorrectionRequest
import com.writebuddy.writebuddy.domain.Correction
import com.writebuddy.writebuddy.domain.FeedbackType
import com.writebuddy.writebuddy.domain.RealExample
import com.writebuddy.writebuddy.repository.CorrectionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class CorrectionService(
    private val correctionRepository: CorrectionRepository,
    private val openAiClient: OpenAiClient,
    private val defaultUserFactory: DefaultUserFactory,
    private val asyncCorrectionService: AsyncCorrectionService
) {
    private val logger: Logger = LoggerFactory.getLogger(CorrectionService::class.java)
    @Transactional
    fun save(request: CorrectionRequest): Correction {
        return save(request, null)
    }

    @Transactional
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

        val defaultUser = defaultUserFactory.getOrCreateDefaultUser()
        correction.assignToUser(defaultUser)

        val savedCorrection = correctionRepository.save(correction)
        logger.info("교정 결과 저장 완료: id={}, feedbackType={}", savedCorrection.id, savedCorrection.feedbackType)

        return savedCorrection
    }
    
    @Transactional
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

        val defaultUser = defaultUserFactory.getOrCreateDefaultUser()
        correction.assignToUser(defaultUser)

        val savedCorrection = correctionRepository.save(correction)
        logger.info("교정 결과 저장 완료: id={}, feedbackType={}, 예시: {}개",
                   savedCorrection.id, savedCorrection.feedbackType, examples.size)

        return Pair(savedCorrection, examples)
    }

    @Transactional
    fun saveWithExamplesAsync(request: CorrectionRequest, userId: Long?): Pair<Correction, List<RealExample>> {
        val totalStartTime = System.currentTimeMillis()
        logger.info("비동기 병렬 교정 요청 시작: {}", request.originSentence)

        val future = asyncCorrectionService.generateCorrectionWithExamplesParallel(request.originSentence)

        try {
            // GPT-5는 느리므로 타임아웃 60초로 설정
            val (correctionData, examples) = future[60, TimeUnit.SECONDS]
            val (corrected, feedback, feedbackTypeStr, score, originTranslation, correctedTranslation) = correctionData

            val processingTime = System.currentTimeMillis() - totalStartTime
            logger.info("비동기 처리 완료: {}ms", processingTime)

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

            val defaultUser = defaultUserFactory.getOrCreateDefaultUser()
            correction.assignToUser(defaultUser)

            val savedCorrection = correctionRepository.save(correction)

            val totalTime = System.currentTimeMillis() - totalStartTime
            logger.info("전체 비동기 처리 완료: {}ms, id={}, 예시: {}개",
                       totalTime, savedCorrection.id, examples.size)

            return Pair(savedCorrection, examples)

        } catch (e: java.util.concurrent.TimeoutException) {
            future.cancel(true) // 비동기 작업 취소
            logger.error("비동기 처리 타임아웃 (60초), 동기 방식으로 폴백")
            return saveWithExamples(request, userId)
        } catch (e: Exception) {
            future.cancel(true) // 비동기 작업 취소
            logger.error("비동기 처리 실패: {}, 동기 방식으로 폴백", e.message)
            return saveWithExamples(request, userId)
        }
    }

    @Transactional(readOnly = true)
    fun getAll(page: Int = 0, size: Int = 20): List<Correction> {
        val startTime = System.currentTimeMillis()

        val result = correctionRepository.findAll(
            PageRequest.of(page, size)
        ).content

        val duration = System.currentTimeMillis() - startTime
        logger.info("교정 목록 조회 완료 (전체 데이터): {}ms, 조회된 건수: {}", duration, result.size)
        return result
    }

    @Transactional(readOnly = true)
    fun getAllLightweight(page: Int = 0, size: Int = 20): List<com.writebuddy.writebuddy.repository.CorrectionLightProjection> {
        val startTime = System.currentTimeMillis()

        val result = correctionRepository.findAllLightweight(
            PageRequest.of(page, size)
        ).content

        val duration = System.currentTimeMillis() - startTime
        logger.info("교정 목록 조회 완료 (경량 데이터): {}ms, 조회된 건수: {}", duration, result.size)
        return result
    }
    
    @Transactional
    fun toggleFavorite(id: Long): Correction {
        val correction = correctionRepository.findById(id)
            .orElseThrow { IllegalArgumentException("교정 결과를 찾을 수 없습니다: $id") }

        correction.toggleFavorite()
        return correctionRepository.save(correction)
    }

    @Transactional(readOnly = true)
    fun getFavorites(): List<Correction> {
        return correctionRepository.findByIsFavoriteTrue()
    }

    @Transactional
    fun updateMemo(id: Long, memo: String?): Correction {
        val correction = correctionRepository.findById(id)
            .orElseThrow { IllegalArgumentException("교정 결과를 찾을 수 없습니다: $id") }

        correction.updateMemo(memo)
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

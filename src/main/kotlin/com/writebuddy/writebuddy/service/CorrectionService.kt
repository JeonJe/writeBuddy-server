package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.controller.dto.request.CorrectionRequest
import com.writebuddy.writebuddy.domain.Correction
import com.writebuddy.writebuddy.domain.ErrorType
import com.writebuddy.writebuddy.repository.CorrectionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CorrectionService (
    private val correctionRepository: CorrectionRepository,
    private val openAiClient : OpenAiClient,
){
    private val logger = LoggerFactory.getLogger(CorrectionService::class.java)
    fun save(request: CorrectionRequest): Correction {
        logger.info("교정 요청 처리 시작: {}", request.originSentence)
        
        val (corrected, feedback, errorTypeStr) = openAiClient.generateCorrectionAndFeedbackWithType(request.originSentence)
        val errorType = parseErrorType(errorTypeStr)

        val correction = Correction(
            originSentence = request.originSentence,
            correctedSentence = corrected,
            feedback = feedback,
            errorType = errorType
        )
        
        val savedCorrection = correctionRepository.save(correction)
        logger.info("교정 결과 저장 완료: id={}, errorType={}", savedCorrection.id, savedCorrection.errorType)
        
        return savedCorrection
    }

    fun getAll(): List<Correction> {
        logger.debug("전체 교정 목록 조회")
        return correctionRepository.findAll()
    }
    
    fun getErrorTypeStatistics(): Map<ErrorType, Long> {
        logger.debug("오류 타입 통계 조회")
        return correctionRepository.findAll()
            .groupBy { it.errorType }
            .mapValues { it.value.size.toLong() }
    }
    
    private fun parseErrorType(errorTypeStr: String): ErrorType {
        return try {
            ErrorType.valueOf(errorTypeStr.uppercase())
        } catch (e: IllegalArgumentException) {
            logger.warn("알 수 없는 오류 타입: {}. SYSTEM으로 설정", errorTypeStr)
            ErrorType.SYSTEM
        }
    }
}

package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.controller.dto.request.CorrectionRequest
import com.writebuddy.writebuddy.domain.Correction
import com.writebuddy.writebuddy.repository.CorrectionRepository
import org.springframework.stereotype.Service

@Service
class CorrectionService (
    private val correctionRepository: CorrectionRepository,
    private val openAiClient : OpenAiClient,
){
    fun save(request: CorrectionRequest): Correction {
        val origin = request.originSentence
        val (corrected, feedback) = openAiClient.generateCorrectionAndFeedback(origin)


        val correction = Correction(
            originSentence = request.originSentence,
            correctedSentence = corrected,
            feedback = feedback
        )
        val save = correctionRepository.save(correction)
        return save
    }

    fun getAll(): List<Correction> {
        return correctionRepository.findAll()


    }
}

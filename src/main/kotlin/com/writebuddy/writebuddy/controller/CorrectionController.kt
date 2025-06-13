package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.request.CorrectionRequest
import com.writebuddy.writebuddy.controller.dto.response.CorrectionResponse
import com.writebuddy.writebuddy.domain.FeedbackType
import com.writebuddy.writebuddy.service.CorrectionService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/corrections")
class CorrectionController(
    private val correctionService: CorrectionService
) {

    @PostMapping()
    fun create(@RequestBody @Valid request: CorrectionRequest) : CorrectionResponse {
        val saved = correctionService.save(request)
        return CorrectionResponse.from(saved)
    }

    @GetMapping
    fun getAll(): List<CorrectionResponse> {
        val corrections = correctionService.getAll()
        return corrections.map { CorrectionResponse.from(it) }
    }
    
    @GetMapping("/statistics")
    fun getStatistics(): Map<FeedbackType, Long> {
        return correctionService.getFeedbackTypeStatistics()
    }
}

package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.request.CorrectionRequest
import com.writebuddy.writebuddy.controller.dto.request.UpdateMemoRequest
import com.writebuddy.writebuddy.controller.dto.response.CorrectionResponse
import com.writebuddy.writebuddy.domain.FeedbackType
import com.writebuddy.writebuddy.service.CorrectionService
import com.writebuddy.writebuddy.service.RealExampleService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/corrections")
class CorrectionController(
    private val correctionService: CorrectionService,
    private val realExampleService: RealExampleService
) {

    @PostMapping()
    fun create(@RequestBody @Valid request: CorrectionRequest) : CorrectionResponse {
        val (saved, examples) = correctionService.saveWithExamples(request, null)
        return CorrectionResponse.from(saved, examples)
    }
    
    @PostMapping("/users/{userId}")
    fun createWithUser(@PathVariable userId: Long, @RequestBody @Valid request: CorrectionRequest) : CorrectionResponse {
        val (saved, examples) = correctionService.saveWithExamples(request, userId)
        return CorrectionResponse.from(saved, examples)
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
    
    @GetMapping("/average-score")
    fun getAverageScore(): Map<String, Double> {
        val averageScore = correctionService.getAverageScore()
        return mapOf("averageScore" to averageScore)
    }
    
    @GetMapping("/dashboard/daily")
    fun getDailyStatistics(): Map<String, Any> {
        return correctionService.getDailyStatistics()
    }
    
    @GetMapping("/dashboard/score-trend")
    fun getScoreTrend(): Map<String, Any> {
        val trend = correctionService.getScoreTrend()
        return mapOf("scoreTrend" to trend)
    }
    
    @GetMapping("/dashboard/error-patterns")
    fun getErrorPatterns(): Map<String, Any> {
        val patterns = correctionService.getErrorPatternAnalysis()
        return mapOf("errorPatterns" to patterns)
    }
    
    @PutMapping("/{id}/favorite")
    fun toggleFavorite(@PathVariable id: Long): CorrectionResponse {
        val correction = correctionService.toggleFavorite(id)
        return CorrectionResponse.from(correction)
    }
    
    @GetMapping("/favorites")
    fun getFavorites(): List<CorrectionResponse> {
        val favorites = correctionService.getFavorites()
        return favorites.map { CorrectionResponse.from(it) }
    }
    
    @PutMapping("/{id}/memo")
    fun updateMemo(@PathVariable id: Long, @RequestBody request: UpdateMemoRequest): CorrectionResponse {
        val correction = correctionService.updateMemo(id, request.memo)
        return CorrectionResponse.from(correction)
    }
    
}

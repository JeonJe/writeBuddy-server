package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.request.CorrectionRequest
import com.writebuddy.writebuddy.controller.dto.request.UpdateMemoRequest
import com.writebuddy.writebuddy.controller.dto.response.CorrectionResponse
import com.writebuddy.writebuddy.controller.dto.response.CorrectionListResponse
import com.writebuddy.writebuddy.service.CorrectionService
import com.writebuddy.writebuddy.service.LearningAnalyticsService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/corrections")
class CorrectionController(
    private val correctionService: CorrectionService,
    private val learningAnalyticsService: LearningAnalyticsService
) {
    private val logger: Logger = LoggerFactory.getLogger(CorrectionController::class.java)

    @PostMapping()
    fun create(@RequestBody @Valid request: CorrectionRequest) : CorrectionResponse {
        logger.info("교정 API 호출 (비동기): {}", request.originSentence)
        val (saved, examples) = correctionService.saveWithExamplesAsync(request, null)
        return CorrectionResponse.from(saved, examples)
    }

    @PostMapping("/users/{userId}")
    fun createWithUser(@PathVariable userId: Long, @RequestBody @Valid request: CorrectionRequest) : CorrectionResponse {
        logger.info("사용자별 교정 API 호출 (비동기): {}", request.originSentence)
        val (saved, examples) = correctionService.saveWithExamplesAsync(request, userId)
        return CorrectionResponse.from(saved, examples)
    }

    @PostMapping("/sync")
    fun createSync(@RequestBody @Valid request: CorrectionRequest) : CorrectionResponse {
        logger.info("동기 교정 API 호출: {}", request.originSentence)
        val (saved, examples) = correctionService.saveWithExamples(request, null)
        return CorrectionResponse.from(saved, examples)
    }

    @GetMapping
    fun getAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "false") lightweight: Boolean
    ): Any {
        val startTime = System.currentTimeMillis()
        logger.info("GET /corrections 요청 시작 - page: {}, size: {}, lightweight: {}", page, size, lightweight)
        
        return if (lightweight) {
            val projections = correctionService.getAllLightweight(page, size)
            val result = projections.map { CorrectionListResponse.from(it) }
            val duration = System.currentTimeMillis() - startTime
            logger.info("GET /corrections (경량) 요청 완료: {}ms, 반환된 건수: {}", duration, result.size)
            result
        } else {
            val corrections = correctionService.getAll(page, size)
            val result = corrections.map { CorrectionResponse.from(it) }
            val duration = System.currentTimeMillis() - startTime
            logger.info("GET /corrections (전체) 요청 완료: {}ms, 반환된 건수: {}", duration, result.size)
            result
        }
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
    
    @GetMapping("/users/{userId}/good-expressions")
    fun getUserGoodExpressions(@PathVariable userId: Long): List<CorrectionResponse> {
        val goodExpressions = learningAnalyticsService.getUserGoodExpressions(userId)
        return goodExpressions.map { CorrectionResponse.from(it) }
    }
    
}

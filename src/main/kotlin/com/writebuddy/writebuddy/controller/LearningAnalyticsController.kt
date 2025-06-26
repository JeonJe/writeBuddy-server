package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.response.UserWeakAreasSummaryResponse
import com.writebuddy.writebuddy.service.LearningAnalyticsService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/analytics")
class LearningAnalyticsController(
    private val learningAnalyticsService: LearningAnalyticsService
) {
    private val logger: Logger = LoggerFactory.getLogger(LearningAnalyticsController::class.java)

    @GetMapping("/users/{userId}/weak-areas")
    fun getUserWeakAreas(@PathVariable userId: Long): ResponseEntity<UserWeakAreasSummaryResponse> {
        logger.info("사용자 약점 분석 요청: userId={}", userId)
        
        val summary = learningAnalyticsService.analyzeUserWeakAreas(userId)
        val response = UserWeakAreasSummaryResponse.from(summary)
        
        logger.info("사용자 약점 분석 완료: userId={}, 약점 수={}", userId, response.topWeakAreas.size)
        
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/users/{userId}/analyze")
    fun triggerAnalysis(@PathVariable userId: Long): ResponseEntity<Map<String, String>> {
        logger.info("사용자 약점 분석 수동 트리거: userId={}", userId)
        
        learningAnalyticsService.analyzeUserWeakAreas(userId)
        
        return ResponseEntity.ok(mapOf(
            "message" to "약점 분석이 완료되었습니다",
            "userId" to userId.toString()
        ))
    }
}
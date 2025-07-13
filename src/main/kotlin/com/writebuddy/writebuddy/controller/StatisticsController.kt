package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.response.UnifiedStatisticsResponse
import com.writebuddy.writebuddy.service.StatisticsService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/statistics")
class StatisticsController(
    private val statisticsService: StatisticsService
) {
    private val logger: Logger = LoggerFactory.getLogger(StatisticsController::class.java)
    
    @GetMapping
    fun getStatistics(): UnifiedStatisticsResponse {
        val startTime = System.currentTimeMillis()
        logger.info("GET /statistics 요청 시작")
        
        val result = statisticsService.getStatistics()
        
        val duration = System.currentTimeMillis() - startTime
        logger.info("GET /statistics 요청 완료: {}ms", duration)
        return result
    }
}
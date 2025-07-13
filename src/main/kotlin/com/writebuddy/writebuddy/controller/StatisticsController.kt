package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.response.UnifiedStatisticsResponse
import com.writebuddy.writebuddy.service.StatisticsService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/statistics")
class StatisticsController(
    private val statisticsService: StatisticsService
) {
    
    @GetMapping
    fun getStatistics(): UnifiedStatisticsResponse {
        return statisticsService.getStatistics()
    }
}
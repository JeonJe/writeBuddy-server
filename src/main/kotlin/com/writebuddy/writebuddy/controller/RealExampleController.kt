package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.request.CreateRealExampleRequest
import com.writebuddy.writebuddy.controller.dto.response.RealExampleResponse
import com.writebuddy.writebuddy.domain.ExampleSourceType
import com.writebuddy.writebuddy.service.RealExampleService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/examples")
class RealExampleController(
    private val realExampleService: RealExampleService
) {
    
    @GetMapping("/search")
    fun searchExamples(@RequestParam keyword: String): List<RealExampleResponse> {
        val examples = realExampleService.searchExamples(keyword)
        return examples.map { RealExampleResponse.from(it) }
    }
    
    @GetMapping("/phrase")
    fun findByPhrase(@RequestParam phrase: String): List<RealExampleResponse> {
        val examples = realExampleService.findExamplesByPhrase(phrase)
        return examples.map { RealExampleResponse.from(it) }
    }
    
    @GetMapping("/random")
    fun getRandomExamples(@RequestParam(defaultValue = "5") count: Int): List<RealExampleResponse> {
        val examples = realExampleService.getRandomExamples(count)
        return examples.map { RealExampleResponse.from(it) }
    }
    
    @GetMapping("/daily")
    fun getDailyRecommendation(): List<RealExampleResponse> {
        val examples = realExampleService.getDailyRecommendation()
        return examples.map { RealExampleResponse.from(it) }
    }
    
    @GetMapping("/source/{sourceType}")
    fun getBySourceType(@PathVariable sourceType: ExampleSourceType): List<RealExampleResponse> {
        val examples = realExampleService.getExamplesBySourceType(sourceType)
        return examples.map { RealExampleResponse.from(it) }
    }
    
    @GetMapping("/difficulty")
    fun getByDifficulty(
        @RequestParam(defaultValue = "1") minDifficulty: Int,
        @RequestParam(defaultValue = "10") maxDifficulty: Int
    ): List<RealExampleResponse> {
        val examples = realExampleService.getExamplesByDifficulty(minDifficulty, maxDifficulty)
        return examples.map { RealExampleResponse.from(it) }
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createExample(@RequestBody @Valid request: CreateRealExampleRequest): RealExampleResponse {
        val example = realExampleService.createExample(request.toEntity())
        return RealExampleResponse.from(example)
    }
}
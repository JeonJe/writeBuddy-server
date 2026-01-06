package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.response.SentencePracticeResponse
import com.writebuddy.writebuddy.service.PracticeService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/practice")
class PracticeController(
    private val practiceService: PracticeService
) {
    private val logger: Logger = LoggerFactory.getLogger(PracticeController::class.java)

    @GetMapping("/sentence")
    fun getSentencePractice(): ResponseEntity<SentencePracticeResponse> {
        logger.info("문장 연습 생성 요청")

        val response = practiceService.generateSentencePractice()

        logger.info("문장 연습 생성 완료: korean={}", response.korean.take(30))

        return ResponseEntity.ok(response)
    }
}

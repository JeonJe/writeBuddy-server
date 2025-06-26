package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.request.ChatRequest
import com.writebuddy.writebuddy.controller.dto.response.ChatResponse
import com.writebuddy.writebuddy.service.OpenAiClient
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
class ChatController(
    private val openAiClient: OpenAiClient
) {
    private val logger: Logger = LoggerFactory.getLogger(ChatController::class.java)

    @PostMapping
    fun chat(@Valid @RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        logger.info("영어 학습 채팅 요청: {}", request.question)
        
        val answer = openAiClient.generateChatResponse(request.question)
        
        val response = ChatResponse(
            question = request.question,
            answer = answer
        )
        
        logger.info("영어 학습 채팅 응답 완료: questionLength={}, answerLength={}", 
                   request.question.length, answer.length)
        
        return ResponseEntity.ok(response)
    }
}
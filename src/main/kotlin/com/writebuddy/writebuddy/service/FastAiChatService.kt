package com.writebuddy.writebuddy.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class FastAiChatService(
    private val chatChatClient: ChatClient,
    private val promptManager: PromptManager
) {
    private val logger: Logger = LoggerFactory.getLogger(FastAiChatService::class.java)

    fun generateChatResponse(question: String): String {
        return try {
            logger.info("Spring AI 채팅 요청: {}", question.take(50))

            val response = chatChatClient.prompt()
                .system(promptManager.getChatSystemPrompt())
                .user(question)
                .call()
                .content() ?: ""

            logger.info("Spring AI 채팅 응답 완료: {}자", response.length)
            response.ifBlank { promptManager.getChatFallback() }

        } catch (e: Exception) {
            logger.error("Spring AI 채팅 API 호출 실패: {}", e.message, e)
            promptManager.getChatFallback()
        }
    }

    fun generateWithCustomPrompt(systemPrompt: String, userPrompt: String): String {
        return try {
            logger.info("Spring AI 커스텀 프롬프트 요청: {}", userPrompt.take(50))

            val response = chatChatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content() ?: ""

            logger.info("Spring AI 커스텀 프롬프트 응답 완료: {}자", response.length)
            response

        } catch (e: Exception) {
            logger.error("Spring AI API 호출 실패: {}", e.message, e)
            throw e
        }
    }

    fun streamChatResponse(question: String): Flux<String> {
        logger.info("Spring AI 스트리밍 채팅 요청: {}", question.take(50))

        return chatChatClient.prompt()
            .system(promptManager.getChatSystemPrompt())
            .user(question)
            .stream()
            .content()
            .doOnComplete { logger.info("Spring AI 스트리밍 채팅 완료") }
            .onErrorResume { e ->
                logger.error("Spring AI 스트리밍 채팅 실패: {}", e.message)
                Flux.just(promptManager.getChatFallback())
            }
    }
}

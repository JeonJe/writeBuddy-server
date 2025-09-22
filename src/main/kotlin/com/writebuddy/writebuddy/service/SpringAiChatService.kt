package com.writebuddy.writebuddy.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

@Service
class SpringAiChatService(
    private val chatClient: ChatClient,
    private val promptManager: PromptManager
) {
    private val logger: Logger = LoggerFactory.getLogger(SpringAiChatService::class.java)

    fun generateChatResponse(question: String): String {
        return try {
            logger.info("Spring AI 채팅 요청: {}", question.take(50))

            val response = chatClient.prompt()
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

}

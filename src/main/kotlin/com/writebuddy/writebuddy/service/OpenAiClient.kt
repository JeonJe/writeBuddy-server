package com.writebuddy.writebuddy.service

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class OpenAiClient(
    private val restClient: RestClient
) {
    fun generateCorrectionAndFeedback(origin: String): Pair<String, String> {
        val messages = listOf(
            mapOf("role" to "system", "content" to "You are an English grammar correction teacher. Please respond in the following format:\nCORRECTED: [corrected sentence]\nFEEDBACK: [explanation in Korean]"),
            mapOf("role" to "user", "content" to "Please correct this English sentence and explain why:\n$origin")
        )

        val request = mapOf(
            "model" to "gpt-3.5-turbo",
            "messages" to messages,
            "temperature" to 0.7
        )

        val response = restClient.post()
            .uri("/chat/completions")
            .body(request)
            .retrieve()
            .body(ChatCompletionResponse::class.java)

        val content = response?.choices?.firstOrNull()?.message?.content ?: ""
        val (corrected, feedback) = parseResponse(content)
        return corrected to feedback
    }

    private fun parseResponse(content: String): Pair<String, String> {
        val lines = content.lines()
        val corrected = lines.find { it.startsWith("CORRECTED:") }?.removePrefix("CORRECTED:")?.trim() 
            ?: lines.find { it.startsWith("교정문:") }?.removePrefix("교정문:")?.trim() 
            ?: content.split("\n").firstOrNull()?.trim() ?: ""
        
        val feedback = lines.find { it.startsWith("FEEDBACK:") }?.removePrefix("FEEDBACK:")?.trim() 
            ?: lines.find { it.startsWith("피드백:") }?.removePrefix("피드백:")?.trim() 
            ?: content.split("\n").drop(1).joinToString(" ").trim()
        
        return corrected to feedback
    }

}

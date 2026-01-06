package com.writebuddy.writebuddy.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringAiConfig {

    @Value("\${spring.ai.openai.api-key}")
    private lateinit var apiKey: String

    @Value("\${spring.ai.openai.chat-model:gpt-4o-mini}")
    private lateinit var chatModelName: String

    @Bean
    fun openAiApi(): OpenAiApi {
        return OpenAiApi(apiKey)
    }

    @Bean
    fun chatChatClient(openAiApi: OpenAiApi): ChatClient {
        val options = OpenAiChatOptions.builder()
            .withModel(chatModelName)
            .withTemperature(if (isReasoningModel(chatModelName)) 1.0 else 0.3)
            .build()
        val model = OpenAiChatModel(openAiApi, options)
        return ChatClient.builder(model).build()
    }

    private fun isReasoningModel(model: String): Boolean {
        return model.startsWith("gpt-5") || model.startsWith("o1") || model.startsWith("o3")
    }
}
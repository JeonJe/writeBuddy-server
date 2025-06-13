package com.writebuddy.writebuddy.service

data class ChatCompletionResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

data class Message(
    val role: String,
    val content: String
)

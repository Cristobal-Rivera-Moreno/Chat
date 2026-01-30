package com.cristobal.chat.models

data class MessageRequest(
    val userId: String,
    val message: Message,
    val lat: Double,
    val long: Double,
    val chatHistory: List<Message>
)

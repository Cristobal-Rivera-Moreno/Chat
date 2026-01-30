package com.cristobal.chat.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val timestamp: Long
)

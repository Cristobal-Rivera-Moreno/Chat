package com.cristobal.chat.storage.DAOs

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.cristobal.chat.models.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages")
    fun getAllMessages(): Flow<List<Message>>

    @Insert
    suspend fun insertAll(vararg messages: Message)

    @Delete
    suspend fun delete(message: Message)

    @Delete
    suspend fun deleteAll(vararg messages: Message)
}
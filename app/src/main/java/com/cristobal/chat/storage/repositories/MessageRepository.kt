package com.cristobal.chat.storage.repositories

import android.content.Context
import android.util.Log
import com.cristobal.chat.models.Message
import kotlinx.coroutines.flow.Flow

class MessageRepository(context: Context) {
    private val obTag = this::class.java.simpleName
    private val messageDao = com.cristobal.chat.storage.rooms.DatabaseBuilder
        .getInstance(context)
        .messageDao()

    suspend fun insertMessages(vararg messages: Message): Result<Unit> {
        return try {
            messageDao.insertAll(*messages)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(obTag, "Error inserting message: ${e.message}")
            Result.failure(e)
        }
    }

    fun getAllMessages(): Result<Flow<List<Message>>> {
        return try {
            Result.success(messageDao.getAllMessages())
        } catch (e: Exception) {
            Log.e(obTag, "Error retrieving messages: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteExpiredMessages(): Result<Unit> {
        return try {
            getAllMessages().onSuccess { flow ->
                flow.collect { msgs ->
                    if (msgs.getOrNull(0)?.timestamp?.let { it  < System.currentTimeMillis() - 24 * 60 * 60 * 1000} == true) {
                        messageDao.deleteAll(*msgs.toTypedArray())
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(obTag, "Error deleting message: ${e.message}")
            Result.failure(e)
        }
    }

}
package com.cristobal.chat.storage.rooms

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cristobal.chat.models.Message

@Database(entities = [Message::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun messageDao(): com.cristobal.chat.storage.DAOs.MessageDao
}
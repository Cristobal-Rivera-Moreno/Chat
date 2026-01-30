package com.cristobal.chat.storage.rooms

import android.content.Context
import androidx.room.Room

object DatabaseBuilder {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "user-database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
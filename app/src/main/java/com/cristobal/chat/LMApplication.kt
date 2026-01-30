package com.cristobal.chat

import android.app.Application

class LMApplication: Application() {

    companion object {
        private var _instance: LMApplication? = null
        fun getInstance(): LMApplication {
            return _instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this
    }

}
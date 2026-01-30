package com.cristobal.chat.viewmodels

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cristobal.chat.LMApplication
import com.cristobal.chat.R
import com.cristobal.chat.api_services.ChatService
import com.cristobal.chat.api_services.RetrofitInstance
import com.cristobal.chat.models.Message
import com.cristobal.chat.models.MessageRequest
import com.cristobal.chat.services.LocationService
import com.cristobal.chat.storage.repositories.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRoomsViewModel: ViewModel() {
    val obTag = this::class.java.simpleName
    private val _messages: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())
    val messages: StateFlow <List<Message>> get() = _messages
    val chatService = RetrofitInstance.buildApi( ChatService::class.java)
    val androidId: String =
       getOrCreateAppUniqueId(LMApplication.getInstance().applicationContext)
    private var  locationService: LocationService = LocationService(LMApplication.getInstance().applicationContext)
    private var lat: Double = 0.0
    private var lon: Double = 0.0
    private val _infoDialogVisibility = MutableStateFlow(false)
    val infoDialogVisibility: StateFlow<Boolean> get() = _infoDialogVisibility
    private val _isJarvisThinking = MutableStateFlow(false)
    val isJarvisThinking: StateFlow<Boolean> get() = _isJarvisThinking

    private val repository = MessageRepository(
        LMApplication.getInstance().applicationContext
    )

    init {
        locationService.getLastLocation { lat, lon ->
            this.lat = lat
            this.lon = lon
        }

        retrieveMessagesFromDatabase()
    }

    private fun retrieveMessagesFromDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getAllMessages()
            result.onSuccess { flow ->
                flow.collect { msgs ->
                    _messages.value = msgs
                }
            }.onFailure {
                Log.e(obTag, "Error retrieving messages: ${it.message}")
            }
        }
    }

    fun getOrCreateAppUniqueId(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        var id = prefs.getString("unique_id", null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit {putString("unique_id", id)}
        }
        return id
    }

    fun sendMessageToAgent(msg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userMessage = Message(
                    content = msg,
                    role = "user",
                    timestamp = System.currentTimeMillis()
                )

                val agentMessage = Message(
                    content = LMApplication.getInstance().applicationContext.getString(R.string.thinking),
                    role = "model",
                    timestamp = System.currentTimeMillis()
                )
                _messages.value += userMessage

                _messages.value += agentMessage

                _isJarvisThinking.value = true
                val response = chatService.sendMsg(
                    MessageRequest(
                        userId = androidId,
                        message = userMessage,
                        lat = lat,
                        long = lon,
                        chatHistory = _messages.value.toList()
                    )
                )
                _isJarvisThinking.value = false
                if (response.isSuccessful) {
                    Log.d(obTag, "Message sent successfully: ${response.body()}")
                    val agentResponse = response.body()?.agent_response ?: ""
                    addMsgToList(agentResponse)
                    saveInDatabase(userMessage, agentResponse)
                } else {
                    Log.e(obTag, "Failed to send message: ${response.errorBody()?.string()}")
                    addMsgToList(
                        LMApplication.getInstance().applicationContext.getString(R.string.error_message)
                    )
                }
            } catch (e: Exception) {
                _isJarvisThinking.value = false
                Log.e(obTag, "Error sending message to agent: ${e.message}")
                addMsgToList(
                    LMApplication.getInstance().applicationContext.getString(R.string.error_message)
                )
            }
        }
    }

    private fun addMsgToList(msg: String) {
        val newMsgList = _messages.value.toMutableList()
        newMsgList.removeAt( newMsgList.size -1)
        newMsgList.add(
            Message(
                id = androidId,
                content = msg,
                role = "model",
                timestamp = System.currentTimeMillis()
            )
        )
        _messages.value = newMsgList
    }

    fun setInfoDialogVisibility(visible: Boolean) {
        _infoDialogVisibility.value = visible
    }

    private fun saveInDatabase(userMessage: Message, agentResponse: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val agentMessage = Message(
                content = agentResponse,
                role = "model",
                timestamp = System.currentTimeMillis()
            )

            val result = repository.insertMessages(userMessage, agentMessage)
            result.onSuccess {
                Log.d(obTag, "Messages saved to database successfully.")
            }.onFailure {
                Log.e(obTag, "Error saving messages: ${it.message}")
            }

        }
    }
}
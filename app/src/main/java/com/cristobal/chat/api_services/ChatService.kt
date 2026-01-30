package com.cristobal.chat.api_services
import com.cristobal.chat.models.Message
import com.cristobal.chat.models.MessageRequest
import com.cristobal.chat.models.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatService {
    @POST("chat")
    suspend fun sendMsg(@Body messageRequest: MessageRequest): Response<MessageResponse>
}
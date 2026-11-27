package cn.edu.sicnu.cs.fsl.mogu.data.remote

import cn.edu.sicnu.cs.fsl.mogu.data.remote.dto.DeepSeekChatRequest
import cn.edu.sicnu.cs.fsl.mogu.data.remote.dto.DeepSeekChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DeepSeekApi {
    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body request: DeepSeekChatRequest,
    ): DeepSeekChatResponse
}

package cn.edu.sicnu.cs.fsl.mogu.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeepSeekChatRequest(
    val model: String,
    val messages: List<DeepSeekMessage>,
    val temperature: Double = 0.1,
    @SerializedName("max_tokens") val maxTokens: Int = 700,
)

data class DeepSeekMessage(
    val role: String,
    /** String 或 List<DeepSeekContentPart> */
    val content: Any,
)

data class DeepSeekContentPart(
    val type: String,
    val text: String? = null,
    @SerializedName("image_url") val imageUrl: DeepSeekImageUrl? = null,
)

data class DeepSeekImageUrl(
    val url: String,
    val detail: String? = "high",
)

data class DeepSeekChatResponse(
    val choices: List<DeepSeekChoice> = emptyList(),
)

data class DeepSeekChoice(
    val message: DeepSeekResponseMessage? = null,
)

data class DeepSeekResponseMessage(
    val content: String? = null,
)

/** 模型返回的业务 JSON。 */
data class AiIdentifyResponse(
    val top3: List<AiCandidate> = emptyList(),
    val uncertain: Boolean = false,
)

data class AiCandidate(
    val name: String = "",
    val confidence: Double = 0.0,
    val reason: String? = null,
)

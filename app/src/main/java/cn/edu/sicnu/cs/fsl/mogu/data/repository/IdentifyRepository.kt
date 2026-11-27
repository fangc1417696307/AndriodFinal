package cn.edu.sicnu.cs.fsl.mogu.data.repository

import android.content.Context
import cn.edu.sicnu.cs.fsl.mogu.data.local.MoguDatabase
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.IdentifyRecordEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.MushroomEntity
import cn.edu.sicnu.cs.fsl.mogu.data.model.IdentifyResult
import cn.edu.sicnu.cs.fsl.mogu.data.model.TopPrediction
import cn.edu.sicnu.cs.fsl.mogu.data.remote.DeepSeekApi
import cn.edu.sicnu.cs.fsl.mogu.data.remote.dto.AiIdentifyResponse
import cn.edu.sicnu.cs.fsl.mogu.data.remote.dto.DeepSeekChatRequest
import cn.edu.sicnu.cs.fsl.mogu.data.remote.dto.DeepSeekContentPart
import cn.edu.sicnu.cs.fsl.mogu.data.remote.dto.DeepSeekImageUrl
import cn.edu.sicnu.cs.fsl.mogu.data.remote.dto.DeepSeekMessage
import cn.edu.sicnu.cs.fsl.mogu.util.AppSettings
import cn.edu.sicnu.cs.fsl.mogu.util.BitmapUtil
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 识别业务：在线 DeepSeek 优先，失败自动回退本地模拟。
 * B 同学可以在这里继续完善 prompt、置信度校准、错误提示等。
 */
class IdentifyRepository(
    private val context: Context,
    private val db: MoguDatabase,
    private val settings: AppSettings,
    private val api: DeepSeekApi,
) {
    private val gson = Gson()

    suspend fun identify(file: File, userId: Long): IdentifyResult = withContext(Dispatchers.IO) {
        val mushrooms = db.mushroomDao().getAllOnce()
        check(mushrooms.isNotEmpty()) { "图鉴数据为空，请重新安装应用" }

        var fallbackReason: String? = null
        val online = if (settings.useOnline && settings.hasApiKey) {
            runCatching {
                identifyOnline(file, mushrooms)
            }.getOrElse { error ->
                fallbackReason = "在线识别失败：${error.message ?: "网络异常"}"
                null
            }
        } else {
            if (settings.useOnline && !settings.hasApiKey) {
                fallbackReason = "未配置 DeepSeek API Key，已使用本地模拟"
            }
            null
        }

        val result = online ?: run {
            val predictions = MockIdentifyEngine.predict(file, mushrooms)
            check(predictions.isNotEmpty()) { "没有可识别的品种" }
            val first = predictions.first()
            IdentifyResult(
                recordId = 0,
                predictedName = first.name,
                mushroomId = first.mushroomId,
                edibleType = first.edibleType,
                confidence = first.confidence,
                top3 = predictions,
                source = "mock",
                fallbackReason = fallbackReason,
            )
        }

        val recordId = db.identifyRecordDao().insert(
            IdentifyRecordEntity(
                userId = userId,
                imagePath = file.absolutePath,
                predictedName = result.predictedName,
                mushroomId = result.mushroomId,
                edibleType = result.edibleType,
                confidence = result.confidence,
                top3Json = gson.toJson(result.top3),
                source = result.source,
            ),
        )
        result.copy(recordId = recordId)
    }

    private suspend fun identifyOnline(file: File, mushrooms: List<MushroomEntity>): IdentifyResult {
        val jpeg = BitmapUtil.compressForUpload(file)
        val prompt = buildPrompt(mushrooms)
        val request = DeepSeekChatRequest(
            model = settings.modelName,
            messages = listOf(
                DeepSeekMessage(
                    role = "user",
                    content = listOf(
                        DeepSeekContentPart(type = "text", text = prompt),
                        DeepSeekContentPart(
                            type = "image_url",
                            imageUrl = DeepSeekImageUrl(url = BitmapUtil.toDataUrl(jpeg), detail = "high"),
                        ),
                    ),
                ),
            ),
            temperature = 0.1,
            maxTokens = 700,
        )
        val response = api.chat("Bearer ${settings.apiKey}", request)
        val content = response.choices.firstOrNull()?.message?.content
            ?: error("模型没有返回内容")
        val ai = gson.fromJson(extractJson(content), AiIdentifyResponse::class.java)
        if (ai == null || ai.uncertain || ai.top3.isEmpty()) {
            error("模型无法确定品种")
        }

        val predictions = ai.top3.take(3).mapNotNull { candidate ->
            val name = candidate.name.trim()
            if (name.isEmpty()) return@mapNotNull null
            val matched = findMushroom(name, mushrooms)
            TopPrediction(
                name = matched?.name ?: name,
                confidence = candidate.confidence.coerceIn(0.0, 1.0),
                edibleType = matched?.edibleType ?: 3,
                mushroomId = matched?.mushroomId,
            )
        }
        check(predictions.isNotEmpty()) { "模型返回的候选为空" }
        val first = predictions.first()
        return IdentifyResult(
            recordId = 0,
            predictedName = first.name,
            mushroomId = first.mushroomId,
            edibleType = first.edibleType,
            confidence = first.confidence,
            top3 = predictions,
            source = "deepseek",
        )
    }

    private fun buildPrompt(mushrooms: List<MushroomEntity>): String {
        val list = mushrooms.joinToString("\n") { mushroom ->
            val alias = mushroom.alias?.takeIf { it.isNotBlank() }?.let { "，别名：$it" }.orEmpty()
            "${mushroom.mushroomId}. ${mushroom.name}$alias（${edibleText(mushroom.edibleType)}）"
        }
        return """
            你是一个蘑菇图鉴辅助工具。用户会提供一张图片。
            请只从下面的品种列表中选择候选，禁止编造列表外的品种：
            $list

            要求：
            1. 最多给出 3 个候选，按置信度从高到低排列。
            2. confidence 为 0 到 1 之间的小数，表示你对该候选的置信度。
            3. 如果图片不是蘑菇，或无法可靠判断，top3 返回空数组并把 uncertain 设为 true。
            4. 不要给出“可食用”结论，只做品种候选。
            5. 只输出 JSON，不要输出 Markdown 代码块或解释文字。
            JSON 格式：
            {"top3":[{"name":"品种名","confidence":0.85,"reason":"简要依据"}],"uncertain":false}
        """.trimIndent()
    }

    private fun findMushroom(name: String, mushrooms: List<MushroomEntity>): MushroomEntity? {
        mushrooms.firstOrNull { it.name == name }?.let { return it }
        mushrooms.firstOrNull { mushroom ->
            mushroom.alias?.split('、', '，', ',')?.any { it.trim() == name } == true
        }?.let { return it }
        return mushrooms.firstOrNull { name.contains(it.name) || it.name.contains(name) }
    }

    private fun edibleText(type: Int): String = when (type) {
        1 -> "可食用"
        2 -> "有毒"
        else -> "待确认"
    }

    private fun extractJson(content: String): String {
        val text = content.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        require(start >= 0 && end > start) { "模型返回格式不正确" }
        return text.substring(start, end + 1)
    }
}

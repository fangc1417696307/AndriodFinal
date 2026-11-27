package cn.edu.sicnu.cs.fsl.mogu.data.repository

import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.MushroomEntity
import cn.edu.sicnu.cs.fsl.mogu.data.model.TopPrediction
import java.io.File
import java.security.MessageDigest
import kotlin.random.Random

/**
 * 本地模拟识别：用图片内容哈希做随机种子，保证同一张图结果稳定。
 * 仅用于离线兜底和演示，不代表真实识别能力。
 */
object MockIdentifyEngine {
    fun predict(file: File, mushrooms: List<MushroomEntity>): List<TopPrediction> {
        if (mushrooms.isEmpty()) return emptyList()
        val seed = seedOf(file.readBytes())
        val random = Random(seed)
        val shuffled = mushrooms.shuffled(random)
        val count = minOf(3, shuffled.size)
        var confidence = 0.90 + random.nextDouble() * 0.09
        return List(count) { index ->
            val mushroom = shuffled[index]
            val item = TopPrediction(
                name = mushroom.name,
                confidence = (confidence * 10000).toInt() / 10000.0,
                edibleType = mushroom.edibleType,
                mushroomId = mushroom.mushroomId,
            )
            confidence = (confidence - (0.08 + random.nextDouble() * 0.08)).coerceAtLeast(0.30)
            item
        }
    }

    private fun seedOf(bytes: ByteArray): Long {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        var seed = 0L
        for (i in 0 until Long.SIZE_BYTES) {
            seed = (seed shl 8) or (hash[i].toLong() and 0xFFL)
        }
        return seed
    }
}

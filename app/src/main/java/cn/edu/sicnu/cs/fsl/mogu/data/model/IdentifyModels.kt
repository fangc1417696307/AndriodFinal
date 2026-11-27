package cn.edu.sicnu.cs.fsl.mogu.data.model

data class TopPrediction(
    val name: String,
    val confidence: Double,
    val edibleType: Int,
    val mushroomId: Int? = null,
)

data class IdentifyResult(
    val recordId: Long,
    val predictedName: String,
    val mushroomId: Int?,
    val edibleType: Int,
    val confidence: Double,
    val top3: List<TopPrediction>,
    /** deepseek / mock */
    val source: String,
    val fallbackReason: String? = null,
)

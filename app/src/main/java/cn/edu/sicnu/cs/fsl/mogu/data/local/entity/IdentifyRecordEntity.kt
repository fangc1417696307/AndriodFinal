package cn.edu.sicnu.cs.fsl.mogu.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "identify_record",
    indices = [Index(value = ["userId", "identifyTime"])],
)
data class IdentifyRecordEntity(
    @PrimaryKey(autoGenerate = true) val recordId: Long = 0,
    val userId: Long,
    /** 本地图片绝对路径；可能是 filesDir/identify 下的文件 */
    val imagePath: String? = null,
    val predictedName: String,
    val mushroomId: Int? = null,
    val edibleType: Int = 3,
    val confidence: Double = 0.0,
    /** Top3 候选 JSON */
    val top3Json: String = "[]",
    val favorite: Int = 0,
    val reviewStatus: Int = 0,
    val reviewRemark: String? = null,
    val identifyTime: Long = System.currentTimeMillis(),
    /** deepseek / mock */
    val source: String = "mock",
)

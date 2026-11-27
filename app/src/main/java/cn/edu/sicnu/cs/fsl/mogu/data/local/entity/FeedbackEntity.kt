package cn.edu.sicnu.cs.fsl.mogu.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "feedback",
    indices = [Index(value = ["userId", "createdAt"])],
)
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true) val feedbackId: Long = 0,
    val userId: Long,
    val recordId: Long? = null,
    val content: String,
    /** 0 待处理，1 已处理 */
    val status: Int = 0,
    val handleRemark: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

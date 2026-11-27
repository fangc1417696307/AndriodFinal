package cn.edu.sicnu.cs.fsl.mogu.data.repository

import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.FeedbackDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.FeedbackEntity
import kotlinx.coroutines.flow.Flow

class FeedbackRepository(
    private val feedbackDao: FeedbackDao,
) {
    fun observeMine(userId: Long): Flow<List<FeedbackEntity>> =
        feedbackDao.observeByUser(userId)

    suspend fun submit(userId: Long, content: String, recordId: Long?): Result<Long> {
        val text = content.trim()
        if (text.isEmpty()) {
            return Result.failure(IllegalArgumentException("请填写反馈内容"))
        }
        val id = feedbackDao.insert(
            FeedbackEntity(
                userId = userId,
                recordId = recordId,
                content = text,
            ),
        )
        return Result.success(id)
    }
}

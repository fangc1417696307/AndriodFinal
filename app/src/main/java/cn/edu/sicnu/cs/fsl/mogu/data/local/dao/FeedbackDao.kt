package cn.edu.sicnu.cs.fsl.mogu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.FeedbackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedbackDao {
    @Query("SELECT * FROM feedback WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeByUser(userId: Long): Flow<List<FeedbackEntity>>

    @Query("SELECT * FROM feedback WHERE feedbackId = :id LIMIT 1")
    suspend fun getById(id: Long): FeedbackEntity?

    @Insert
    suspend fun insert(feedback: FeedbackEntity): Long

    @Query("SELECT COUNT(*) FROM feedback WHERE userId = :userId")
    suspend fun countByUser(userId: Long): Int
}

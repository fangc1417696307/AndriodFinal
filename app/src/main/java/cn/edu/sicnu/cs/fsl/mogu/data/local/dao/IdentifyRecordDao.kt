package cn.edu.sicnu.cs.fsl.mogu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.IdentifyRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdentifyRecordDao {
    @Query("SELECT * FROM identify_record WHERE userId = :userId ORDER BY identifyTime DESC")
    fun observeByUser(userId: Long): Flow<List<IdentifyRecordEntity>>

    @Query("SELECT * FROM identify_record WHERE recordId = :recordId AND userId = :userId LIMIT 1")
    suspend fun getByIdForUser(recordId: Long, userId: Long): IdentifyRecordEntity?

    @Query("SELECT * FROM identify_record WHERE recordId = :recordId LIMIT 1")
    suspend fun getById(recordId: Long): IdentifyRecordEntity?

    @Insert
    suspend fun insert(record: IdentifyRecordEntity): Long

    @Query("UPDATE identify_record SET favorite = :favorite WHERE recordId = :recordId AND userId = :userId")
    suspend fun updateFavorite(recordId: Long, userId: Long, favorite: Int)

    @Query("DELETE FROM identify_record WHERE recordId = :recordId AND userId = :userId")
    suspend fun delete(recordId: Long, userId: Long)

    @Query("SELECT COUNT(*) FROM identify_record WHERE userId = :userId")
    suspend fun countByUser(userId: Long): Int
}

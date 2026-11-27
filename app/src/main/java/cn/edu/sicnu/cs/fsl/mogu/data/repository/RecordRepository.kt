package cn.edu.sicnu.cs.fsl.mogu.data.repository

import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.IdentifyRecordDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.IdentifyRecordEntity
import kotlinx.coroutines.flow.Flow

class RecordRepository(
    private val recordDao: IdentifyRecordDao,
) {
    fun observeRecords(userId: Long): Flow<List<IdentifyRecordEntity>> =
        recordDao.observeByUser(userId)

    suspend fun getRecord(recordId: Long, userId: Long): IdentifyRecordEntity? =
        recordDao.getByIdForUser(recordId, userId)

    suspend fun toggleFavorite(recordId: Long, userId: Long): Boolean {
        val record = recordDao.getByIdForUser(recordId, userId) ?: return false
        val next = if (record.favorite == 1) 0 else 1
        recordDao.updateFavorite(recordId, userId, next)
        return next == 1
    }

    suspend fun delete(recordId: Long, userId: Long) {
        recordDao.delete(recordId, userId)
    }
}

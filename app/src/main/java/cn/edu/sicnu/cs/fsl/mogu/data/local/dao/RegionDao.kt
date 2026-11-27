package cn.edu.sicnu.cs.fsl.mogu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.RegionEntity

@Dao
interface RegionDao {
    @Query("SELECT * FROM region ORDER BY code")
    suspend fun getAll(): List<RegionEntity>

    @Query("SELECT * FROM region WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): RegionEntity?

    @Query("SELECT COUNT(*) FROM region")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RegionEntity>)
}

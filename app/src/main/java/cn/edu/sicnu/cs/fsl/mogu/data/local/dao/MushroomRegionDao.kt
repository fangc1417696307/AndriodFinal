package cn.edu.sicnu.cs.fsl.mogu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.MushroomRegionEntity

@Dao
interface MushroomRegionDao {
    @Query("SELECT COUNT(*) FROM mushroom_region")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MushroomRegionEntity>)
}

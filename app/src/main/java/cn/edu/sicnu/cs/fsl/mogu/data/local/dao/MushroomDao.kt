package cn.edu.sicnu.cs.fsl.mogu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.MushroomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MushroomDao {
    @Query("SELECT * FROM mushroom ORDER BY name")
    fun observeAll(): Flow<List<MushroomEntity>>

    @Query(
        """
        SELECT * FROM mushroom
        WHERE (:keyword = '' OR name LIKE '%' || :keyword || '%'
               OR IFNULL(alias, '') LIKE '%' || :keyword || '%')
          AND (:edibleType = 0 OR edibleType = :edibleType)
        ORDER BY CASE edibleType WHEN 1 THEN 0 WHEN 2 THEN 1 ELSE 2 END, name
        """
    )
    fun observeFiltered(keyword: String, edibleType: Int): Flow<List<MushroomEntity>>

    @Query("SELECT * FROM mushroom ORDER BY name")
    suspend fun getAllOnce(): List<MushroomEntity>

    @Query("SELECT * FROM mushroom WHERE mushroomId = :id LIMIT 1")
    suspend fun getById(id: Int): MushroomEntity?

    @Query(
        """
        SELECT * FROM mushroom
        WHERE categoryCode = :categoryCode AND mushroomId != :excludeId
        ORDER BY viewCount DESC, name
        LIMIT :limit
        """
    )
    suspend fun getRelated(categoryCode: String, excludeId: Int, limit: Int = 6): List<MushroomEntity>

    @Query(
        """
        SELECT m.* FROM mushroom m
        INNER JOIN mushroom_region r ON m.mushroomId = r.mushroomId
        WHERE r.regionCode IN (:regionCodes) AND m.edibleType = :edibleType
        ORDER BY m.name
        """
    )
    suspend fun getByRegions(regionCodes: List<String>, edibleType: Int): List<MushroomEntity>

    @Query("SELECT * FROM mushroom WHERE edibleType = :edibleType ORDER BY name")
    suspend fun getByEdibleType(edibleType: Int): List<MushroomEntity>

    @Query("SELECT COUNT(*) FROM mushroom")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MushroomEntity>)

    @Query("UPDATE mushroom SET viewCount = viewCount + 1 WHERE mushroomId = :id")
    suspend fun increaseViewCount(id: Int)
}

package cn.edu.sicnu.cs.fsl.mogu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.CategoryEntity

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY sort, name")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM category")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CategoryEntity>)
}

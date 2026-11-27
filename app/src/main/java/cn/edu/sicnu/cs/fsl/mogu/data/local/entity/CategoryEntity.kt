package cn.edu.sicnu.cs.fsl.mogu.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category",
    indices = [Index(value = ["code"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val categoryId: Long = 0,
    val code: String,
    val name: String,
    val sort: Int = 0,
    val remark: String? = null,
)

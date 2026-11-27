package cn.edu.sicnu.cs.fsl.mogu.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 图鉴品种。mushroomId 来自内置种子数据，不使用自增。
 */
@Entity(
    tableName = "mushroom",
    indices = [
        Index("name"),
        Index("categoryCode"),
        Index("edibleType"),
    ],
)
data class MushroomEntity(
    @PrimaryKey val mushroomId: Int,
    val name: String,
    val alias: String? = null,
    val categoryCode: String,
    val categoryName: String,
    val edibleType: Int = 3,
    val growthSeason: String? = null,
    val toxicityDesc: String? = null,
    val edibleAdvice: String? = null,
    val content: String? = null,
    val morphology: String? = null,
    val distribution: String? = null,
    val coverImage: String? = null,
    val viewCount: Int = 0,
    val status: Int = 1,
)

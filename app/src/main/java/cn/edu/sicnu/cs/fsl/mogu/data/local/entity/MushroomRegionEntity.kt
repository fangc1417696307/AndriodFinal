package cn.edu.sicnu.cs.fsl.mogu.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mushroom_region",
    indices = [
        Index(value = ["mushroomId", "regionCode"], unique = true),
        Index("regionCode"),
    ],
)
data class MushroomRegionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mushroomId: Int,
    val regionCode: String,
)

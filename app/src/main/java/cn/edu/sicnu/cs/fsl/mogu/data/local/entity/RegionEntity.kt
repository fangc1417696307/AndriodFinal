package cn.edu.sicnu.cs.fsl.mogu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "region")
data class RegionEntity(
    @PrimaryKey val code: String,
    val name: String,
    val macro: String,
    /** 城市列表 JSON，结构为 [{"code":"...","name":"..."}] */
    val citiesJson: String,
)

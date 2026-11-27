package cn.edu.sicnu.cs.fsl.mogu.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 本地用户。密码只保存加盐哈希，不保存明文。
 */
@Entity(
    tableName = "user",
    indices = [Index(value = ["username"], unique = true)],
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    val username: String,
    val passwordHash: String,
    val passwordSalt: String,
    val realName: String,
    val phone: String? = null,
    val avatar: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

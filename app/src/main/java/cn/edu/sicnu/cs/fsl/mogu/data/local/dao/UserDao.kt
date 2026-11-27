package cn.edu.sicnu.cs.fsl.mogu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Query("SELECT * FROM user WHERE userId = :userId LIMIT 1")
    suspend fun findById(userId: Long): UserEntity?

    @Query("SELECT COUNT(*) FROM user")
    suspend fun count(): Int

    @Insert
    suspend fun insert(user: UserEntity): Long

    @Query("UPDATE user SET realName = :realName, phone = :phone, avatar = :avatar WHERE userId = :userId")
    suspend fun updateProfile(userId: Long, realName: String, phone: String?, avatar: String?)

    @Query("UPDATE user SET passwordHash = :hash, passwordSalt = :salt WHERE userId = :userId")
    suspend fun updatePassword(userId: Long, hash: String, salt: String)
}

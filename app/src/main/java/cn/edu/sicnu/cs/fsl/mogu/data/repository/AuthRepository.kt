package cn.edu.sicnu.cs.fsl.mogu.data.repository

import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.UserDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.UserEntity
import cn.edu.sicnu.cs.fsl.mogu.util.PasswordHasher
import cn.edu.sicnu.cs.fsl.mogu.util.SessionManager

class AuthRepository(
    private val userDao: UserDao,
    private val session: SessionManager,
) {
    suspend fun login(username: String, password: String): Result<UserEntity> {
        val name = username.trim()
        if (name.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("请输入用户名和密码"))
        }
        val user = userDao.findByUsername(name)
            ?: return Result.failure(IllegalArgumentException("用户名或密码错误"))
        if (!PasswordHasher.verify(password, user.passwordSalt, user.passwordHash)) {
            return Result.failure(IllegalArgumentException("用户名或密码错误"))
        }
        session.save(user.userId, user.username)
        return Result.success(user)
    }

    suspend fun register(username: String, password: String): Result<UserEntity> {
        val name = username.trim()
        if (name.length !in 3..20) {
            return Result.failure(IllegalArgumentException("用户名需为 3-20 位"))
        }
        if (password.length !in 6..32) {
            return Result.failure(IllegalArgumentException("密码需为 6-32 位"))
        }
        if (userDao.findByUsername(name) != null) {
            return Result.failure(IllegalArgumentException("用户名已存在"))
        }
        val salt = PasswordHasher.newSalt()
        val user = UserEntity(
            username = name,
            passwordHash = PasswordHasher.hash(password, salt),
            passwordSalt = salt,
            // 注册只填用户名和密码，姓名先默认使用用户名，之后可在个人中心修改。
            realName = name,
        )
        val id = userDao.insert(user)
        val saved = user.copy(userId = id)
        session.save(id, saved.username)
        return Result.success(saved)
    }

    suspend fun currentUser(): UserEntity? {
        val id = session.userId
        if (id <= 0) return null
        return userDao.findById(id)
    }

    suspend fun updateProfile(realName: String, phone: String?, avatar: String?) {
        val id = session.userId
        if (id > 0) userDao.updateProfile(id, realName.trim(), phone?.trim(), avatar?.trim())
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        val user = currentUser() ?: return Result.failure(IllegalStateException("登录状态已失效"))
        if (!PasswordHasher.verify(oldPassword, user.passwordSalt, user.passwordHash)) {
            return Result.failure(IllegalArgumentException("原密码不正确"))
        }
        if (newPassword.length !in 6..32) {
            return Result.failure(IllegalArgumentException("新密码需为 6-32 位"))
        }
        val salt = PasswordHasher.newSalt()
        userDao.updatePassword(user.userId, PasswordHasher.hash(newPassword, salt), salt)
        return Result.success(Unit)
    }

    fun logout() = session.clear()
}

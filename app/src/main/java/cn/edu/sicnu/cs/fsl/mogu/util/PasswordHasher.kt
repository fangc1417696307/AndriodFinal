package cn.edu.sicnu.cs.fsl.mogu.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * 本地课程的密码哈希：随机盐 + SHA-256 多轮迭代。
 * 不保存明文密码。
 */
object PasswordHasher {
    private const val ITERATIONS = 10_000

    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        var data = (salt + password).toByteArray(Charsets.UTF_8)
        repeat(ITERATIONS) {
            data = digest.digest(data)
        }
        return Base64.encodeToString(data, Base64.NO_WRAP)
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean {
        return hash(password, salt) == expectedHash
    }
}

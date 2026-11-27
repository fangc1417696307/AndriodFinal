package cn.edu.sicnu.cs.fsl.mogu.util

import android.content.Context

/**
 * 登录态只保存用户 id 和用户名，token 类信息不需要放在 Room。
 */
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("mogu_session", Context.MODE_PRIVATE)

    val userId: Long
        get() = prefs.getLong(KEY_USER_ID, 0L)

    val username: String?
        get() = prefs.getString(KEY_USERNAME, null)

    val isLoggedIn: Boolean
        get() = userId > 0

    fun save(userId: Long, username: String) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_USERNAME = "username"
    }
}

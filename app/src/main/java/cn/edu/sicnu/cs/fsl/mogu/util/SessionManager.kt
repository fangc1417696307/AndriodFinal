package cn.edu.sicnu.cs.fsl.mogu.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 登录态只保存用户 id 和用户名，token 类信息不需要放在 Room。
 * 同时暴露 Flow，页面可以在登录/退出后自动刷新。
 */
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("mogu_session", Context.MODE_PRIVATE)

    private val _userIdFlow = MutableStateFlow(prefs.getLong(KEY_USER_ID, 0L))
    val userIdFlow: StateFlow<Long> = _userIdFlow.asStateFlow()

    private val _isLoggedInFlow = MutableStateFlow(_userIdFlow.value > 0)
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedInFlow.asStateFlow()

    val userId: Long
        get() = _userIdFlow.value

    val username: String?
        get() = prefs.getString(KEY_USERNAME, null)

    val isLoggedIn: Boolean
        get() = userId > 0

    fun save(userId: Long, username: String) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .apply()
        _userIdFlow.value = userId
        _isLoggedInFlow.value = true
    }

    fun clear() {
        prefs.edit().clear().apply()
        _userIdFlow.value = 0L
        _isLoggedInFlow.value = false
    }

    private companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_USERNAME = "username"
    }
}

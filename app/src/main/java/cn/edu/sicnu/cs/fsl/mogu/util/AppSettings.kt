package cn.edu.sicnu.cs.fsl.mogu.util

import android.content.Context

/**
 * 识别相关的本地设置。API Key 只存在本机 SharedPreferences，不进入仓库。
 */
class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("mogu_settings", Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_API_KEY, value.trim()).apply()

    var useOnline: Boolean
        get() = prefs.getBoolean(KEY_ONLINE, true)
        set(value) = prefs.edit().putBoolean(KEY_ONLINE, value).apply()

    val hasApiKey: Boolean
        get() = apiKey.isNotBlank()

    val modelName: String
        get() = "deepseek-flash"

    /** 本地菌讯选择的省份。 */
    var regionCode: String
        get() = prefs.getString(KEY_REGION_CODE, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_REGION_CODE, value).apply()

    var regionName: String
        get() = prefs.getString(KEY_REGION_NAME, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_REGION_NAME, value).apply()

    var regionMacro: String
        get() = prefs.getString(KEY_REGION_MACRO, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_REGION_MACRO, value).apply()

    val hasRegion: Boolean
        get() = regionCode.isNotBlank() && regionMacro.isNotBlank()

    fun saveRegion(code: String, name: String, macro: String) {
        prefs.edit()
            .putString(KEY_REGION_CODE, code)
            .putString(KEY_REGION_NAME, name)
            .putString(KEY_REGION_MACRO, macro)
            .apply()
    }

    private companion object {
        const val KEY_API_KEY = "deepseek_api_key"
        const val KEY_ONLINE = "use_online"
        const val KEY_REGION_CODE = "region_code"
        const val KEY_REGION_NAME = "region_name"
        const val KEY_REGION_MACRO = "region_macro"
    }
}

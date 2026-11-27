package cn.edu.sicnu.cs.fsl.mogu

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import cn.edu.sicnu.cs.fsl.mogu.data.local.seed.SeedImporter
import kotlinx.coroutines.runBlocking

class MoguApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ServiceLocator.init(this)
        // 数据量很小，首次启动同步导入，确保登录页和首页打开时 Room 已就绪。
        runCatching {
            runBlocking { SeedImporter.seedIfNeeded(this@MoguApp, ServiceLocator.db) }
        }.onFailure {
            Log.e("MoguApp", "种子数据导入失败", it)
        }
    }
}

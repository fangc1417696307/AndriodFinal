package cn.edu.sicnu.cs.fsl.mogu

import android.content.Context
import cn.edu.sicnu.cs.fsl.mogu.data.local.MoguDatabase
import cn.edu.sicnu.cs.fsl.mogu.data.remote.DeepSeekClient
import cn.edu.sicnu.cs.fsl.mogu.data.repository.AuthRepository
import cn.edu.sicnu.cs.fsl.mogu.data.repository.FeedbackRepository
import cn.edu.sicnu.cs.fsl.mogu.data.repository.IdentifyRepository
import cn.edu.sicnu.cs.fsl.mogu.data.repository.MushroomRepository
import cn.edu.sicnu.cs.fsl.mogu.data.repository.RecordRepository
import cn.edu.sicnu.cs.fsl.mogu.util.AppSettings
import cn.edu.sicnu.cs.fsl.mogu.util.SessionManager

/**
 * 轻量级手写依赖容器，避免课程项目引入 Hilt 等额外复杂度。
 */
object ServiceLocator {
    lateinit var appContext: Context
        private set
    lateinit var db: MoguDatabase
        private set
    lateinit var session: SessionManager
        private set
    lateinit var settings: AppSettings
        private set

    val authRepository: AuthRepository by lazy { AuthRepository(db.userDao(), session) }
    val mushroomRepository: MushroomRepository by lazy {
        MushroomRepository(db.mushroomDao(), db.categoryDao(), db.regionDao(), db.searchHistoryDao())
    }
    val recordRepository: RecordRepository by lazy { RecordRepository(db.identifyRecordDao()) }
    val feedbackRepository: FeedbackRepository by lazy { FeedbackRepository(db.feedbackDao()) }
    val identifyRepository: IdentifyRepository by lazy {
        IdentifyRepository(appContext, db, settings, DeepSeekClient.create())
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        db = MoguDatabase.get(appContext)
        session = SessionManager(appContext)
        settings = AppSettings(appContext)
    }
}

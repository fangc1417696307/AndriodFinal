package cn.edu.sicnu.cs.fsl.mogu.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.CategoryDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.FeedbackDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.IdentifyRecordDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.MushroomDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.MushroomRegionDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.RegionDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.SearchHistoryDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.UserDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.CategoryEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.FeedbackEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.IdentifyRecordEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.MushroomEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.MushroomRegionEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.RegionEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.SearchHistoryEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        MushroomEntity::class,
        MushroomRegionEntity::class,
        RegionEntity::class,
        IdentifyRecordEntity::class,
        FeedbackEntity::class,
        SearchHistoryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class MoguDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun mushroomDao(): MushroomDao
    abstract fun mushroomRegionDao(): MushroomRegionDao
    abstract fun regionDao(): RegionDao
    abstract fun identifyRecordDao(): IdentifyRecordDao
    abstract fun feedbackDao(): FeedbackDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var instance: MoguDatabase? = null

        fun get(context: Context): MoguDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MoguDatabase::class.java,
                    "mogu.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}

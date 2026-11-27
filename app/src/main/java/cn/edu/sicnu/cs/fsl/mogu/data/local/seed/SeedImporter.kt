package cn.edu.sicnu.cs.fsl.mogu.data.local.seed

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import cn.edu.sicnu.cs.fsl.mogu.data.local.MoguDatabase
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.CategoryEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.MushroomEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.MushroomRegionEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.RegionEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.UserEntity
import cn.edu.sicnu.cs.fsl.mogu.util.PasswordHasher
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 首次启动把 assets/seed 下的 JSON 导入 Room，并创建演示账号。
 * 骨架阶段同步执行（数据量很小），后续可改到启动页异步加载。
 */
object SeedImporter {
    private const val TAG = "SeedImporter"
    private val gson = Gson()

    suspend fun seedIfNeeded(context: Context, db: MoguDatabase) = withContext(Dispatchers.IO) {
        seedMushrooms(context, db)
        seedRegions(context, db)
        ensureDemoUser(db)
        Log.i(
            TAG,
            "seed ready: mushrooms=${db.mushroomDao().count()}, regions=${db.regionDao().count()}, users=${db.userDao().count()}",
        )
    }

    private suspend fun seedMushrooms(context: Context, db: MoguDatabase) {
        if (db.mushroomDao().count() > 0) return

        val categories = gson.fromJson(
            readAsset(context, "seed/categories.json"),
            Array<SeedCategory>::class.java,
        ).orEmpty().map {
            CategoryEntity(
                code = it.code,
                name = it.name,
                sort = it.sort,
                remark = it.remark,
            )
        }
        val mushrooms = gson.fromJson(
            readAsset(context, "seed/mushrooms.json"),
            Array<SeedMushroom>::class.java,
        ).orEmpty()
        val mushroomEntities = mushrooms.map {
            MushroomEntity(
                mushroomId = it.mushroomId,
                name = it.name,
                alias = it.alias,
                categoryCode = it.categoryCode,
                categoryName = it.categoryName,
                edibleType = it.edibleType,
                growthSeason = it.growthSeason,
                toxicityDesc = it.toxicityDesc,
                edibleAdvice = it.edibleAdvice,
                content = it.content,
                morphology = it.morphology,
                distribution = it.distribution,
                coverImage = it.coverImage,
            )
        }
        val regionLinks = mushrooms.flatMap { mushroom ->
            mushroom.regions.map { code ->
                MushroomRegionEntity(mushroomId = mushroom.mushroomId, regionCode = code)
            }
        }

        db.withTransaction {
            db.categoryDao().insertAll(categories)
            db.mushroomDao().insertAll(mushroomEntities)
            db.mushroomRegionDao().insertAll(regionLinks)
        }
        Log.i(TAG, "imported ${mushroomEntities.size} mushrooms and ${categories.size} categories")
    }

    private suspend fun seedRegions(context: Context, db: MoguDatabase) {
        if (db.regionDao().count() > 0) return
        val file = gson.fromJson(
            readAsset(context, "seed/regions.json"),
            SeedRegionsFile::class.java,
        ) ?: return
        val regions = file.provinces.map {
            RegionEntity(
                code = it.code,
                name = it.name,
                macro = it.macro,
                citiesJson = gson.toJson(it.cities),
            )
        }
        db.withTransaction {
            db.regionDao().insertAll(regions)
        }
        Log.i(TAG, "imported ${regions.size} provinces")
    }

    private suspend fun ensureDemoUser(db: MoguDatabase) {
        if (db.userDao().count() > 0) return
        val salt = PasswordHasher.newSalt()
        db.userDao().insert(
            UserEntity(
                username = "demo",
                passwordHash = PasswordHasher.hash("123456", salt),
                passwordSalt = salt,
                realName = "演示用户",
            ),
        )
    }

    private fun readAsset(context: Context, path: String): String =
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
}

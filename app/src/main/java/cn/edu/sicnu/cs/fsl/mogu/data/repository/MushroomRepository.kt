package cn.edu.sicnu.cs.fsl.mogu.data.repository

import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.CategoryDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.MushroomDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.RegionDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.dao.SearchHistoryDao
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.CategoryEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.MushroomEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.RegionEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

class MushroomRepository(
    private val mushroomDao: MushroomDao,
    private val categoryDao: CategoryDao,
    private val regionDao: RegionDao,
    private val searchHistoryDao: SearchHistoryDao,
) {
    fun observeMushrooms(keyword: String, edibleType: Int): Flow<List<MushroomEntity>> =
        mushroomDao.observeFiltered(keyword.trim(), edibleType)

    suspend fun getById(id: Int): MushroomEntity? {
        mushroomDao.increaseViewCount(id)
        return mushroomDao.getById(id)
    }

    suspend fun getRelated(categoryCode: String, excludeId: Int): List<MushroomEntity> =
        mushroomDao.getRelated(categoryCode, excludeId)

    suspend fun getCategories(): List<CategoryEntity> = categoryDao.getAll()

    suspend fun getRegions(): List<RegionEntity> = regionDao.getAll()

    suspend fun getLocalMushrooms(region: RegionEntity, edibleType: Int): List<MushroomEntity> =
        mushroomDao.getByRegions(listOf(region.macro, "NATIONAL"), edibleType)

    suspend fun saveSearch(keyword: String) {
        val text = keyword.trim()
        if (text.isNotEmpty()) {
            searchHistoryDao.insert(SearchHistoryEntity(keyword = text))
        }
    }

    fun observeSearchHistory(): Flow<List<SearchHistoryEntity>> =
        searchHistoryDao.observeRecent()
}

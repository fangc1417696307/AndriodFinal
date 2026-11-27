package cn.edu.sicnu.cs.fsl.mogu.ui.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.MushroomEntity
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.RegionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LocalUiState(
    val regions: List<RegionEntity> = emptyList(),
    val selected: RegionEntity? = null,
    val edible: List<MushroomEntity> = emptyList(),
    val toxic: List<MushroomEntity> = emptyList(),
    val loading: Boolean = false,
)

class LocalViewModel : ViewModel() {
    private val _state = MutableStateFlow(LocalUiState())
    val state = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val regions = ServiceLocator.mushroomRepository.getRegions()
            val savedCode = ServiceLocator.settings.regionCode
            val selected = regions.firstOrNull { it.code == savedCode }
            _state.update { it.copy(regions = regions, selected = selected) }
            if (selected != null) loadLocal(selected)
        }
    }

    fun select(region: RegionEntity) {
        ServiceLocator.settings.saveRegion(region.code, region.name, region.macro)
        _state.update { it.copy(selected = region) }
        loadLocal(region)
    }

    private fun loadLocal(region: RegionEntity) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val edible = ServiceLocator.mushroomRepository.getLocalMushrooms(region, 1)
            val toxic = ServiceLocator.mushroomRepository.getLocalMushrooms(region, 2)
            _state.update { it.copy(loading = false, edible = edible, toxic = toxic) }
        }
    }
}

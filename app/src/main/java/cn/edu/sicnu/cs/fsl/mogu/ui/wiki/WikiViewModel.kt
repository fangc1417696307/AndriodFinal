package cn.edu.sicnu.cs.fsl.mogu.ui.wiki

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.MushroomEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WikiViewModel : ViewModel() {
    private val keyword = MutableStateFlow("")
    private val edibleType = MutableStateFlow(0)

    val mushrooms = combine(keyword, edibleType) { text, type -> text to type }
        .flatMapLatest { (text, type) ->
            ServiceLocator.mushroomRepository.observeMushrooms(text, type)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<MushroomEntity>())

    fun setKeyword(value: String) {
        keyword.value = value
    }

    fun setEdibleType(value: Int) {
        edibleType.value = value
    }

    fun saveSearch() {
        val text = keyword.value
        if (text.isNotBlank()) {
            viewModelScope.launch { ServiceLocator.mushroomRepository.saveSearch(text) }
        }
    }
}

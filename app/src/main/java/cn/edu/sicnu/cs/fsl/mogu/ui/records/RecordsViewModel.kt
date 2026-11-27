package cn.edu.sicnu.cs.fsl.mogu.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.IdentifyRecordEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecordsViewModel : ViewModel() {
    val records = ServiceLocator.session.userIdFlow
        .flatMapLatest { userId ->
            ServiceLocator.recordRepository.observeRecords(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<IdentifyRecordEntity>())

    fun toggleFavorite(recordId: Long) {
        val userId = ServiceLocator.session.userId
        if (userId <= 0) return
        viewModelScope.launch {
            ServiceLocator.recordRepository.toggleFavorite(recordId, userId)
        }
    }
}

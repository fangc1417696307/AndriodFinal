package cn.edu.sicnu.cs.fsl.mogu.ui.identify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.data.model.IdentifyResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class IdentifyUiState(
    val loading: Boolean = false,
    val result: IdentifyResult? = null,
    val error: String? = null,
)

class IdentifyViewModel : ViewModel() {
    private val _state = MutableStateFlow(IdentifyUiState())
    val state = _state.asStateFlow()

    fun identify(file: File) {
        if (_state.value.loading) return
        val userId = ServiceLocator.session.userId
        if (userId <= 0) {
            _state.update { it.copy(error = "登录后才能识别并保存记录") }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching {
                ServiceLocator.identifyRepository.identify(file, userId)
            }.onSuccess { result ->
                _state.update { it.copy(loading = false, result = result) }
            }.onFailure { error ->
                _state.update {
                    it.copy(loading = false, error = error.message ?: "识别失败，请稍后重试")
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

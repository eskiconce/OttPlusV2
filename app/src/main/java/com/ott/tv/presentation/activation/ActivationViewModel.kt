package com.ott.tv.presentation.activation

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ott.tv.domain.ActivationUiState
import com.ott.tv.domain.ActivationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ActivationViewModel(private val activationUseCase: ActivationUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<ActivationUiState>(ActivationUiState.Loading("Conectando…"))
    val uiState: StateFlow<ActivationUiState> = _uiState.asStateFlow()

    fun startActivation() {
        viewModelScope.launch {
            activationUseCase(getDeviceName()).collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER ?: "Unknown"
        val model = Build.MODEL ?: "Android TV"
        return "$manufacturer $model"
    }
}

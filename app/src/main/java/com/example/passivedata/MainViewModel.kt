package com.example.passivedata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PassiveDataRepository,
    private val healthServicesManager: HealthServicesManager
): ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Startup)
    val uiState: StateFlow<UiState> = _uiState

    val passiveDataEnabled: Flow<Boolean>
    val latestHeartRate = repository.lastestHeartRate


    init {

        viewModelScope.launch {
            _uiState.value = if (healthServicesManager.hasHeartRateCapability()) {
                UiState.HeartRateAvailable
            } else {
                UiState.HeartRateNotAvailable
            }
        }

        passiveDataEnabled = repository.passiveDataEnabled
            .distinctUntilChanged()
            .onEach { enabled ->
                viewModelScope.launch {
                    if (enabled)
                        healthServicesManager.registerForHeartRateData()
                    else
                        healthServicesManager.unregisterForHeartRateData()
                }
            }
    }

    fun togglePassiveData(enabled: Boolean) {
        viewModelScope.launch {
            repository.setPassiveDataEnabled(enabled)
        }
    }
}

sealed class UiState {
    object Startup: UiState()
    object HeartRateAvailable: UiState()
    object HeartRateNotAvailable: UiState()
}

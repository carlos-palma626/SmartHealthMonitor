package com.cmpm.tv.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmpm.tv.domain.model.TvUiState
import com.cmpm.tv.domain.repository.SmartHealthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TvViewModel(
    private val repository: SmartHealthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TvUiState())
    val state: StateFlow<TvUiState> = _state.asStateFlow()

    init {
        // Observar historial reactivo del Room DAO (or Fake Repository)
        viewModelScope.launch {
            repository.obtenerHistorial()
                .catch { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
                .collect { lecturas ->
                    _state.update { it.copy(lecturas = lecturas, isLoading = false) }
                }
        }
        // Observar FC actual (StateFlow del sensor o mock)
        viewModelScope.launch {
            repository.fcActual.collect { bpm ->
                _state.update { it.copy(fcActual = bpm) }
            }
        }
    }
}

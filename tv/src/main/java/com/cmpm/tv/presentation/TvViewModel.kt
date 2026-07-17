package com.cmpm.tv.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.cmpm.tv.domain.model.TvUiState
import com.cmpm.tv.domain.model.LecturaFC
import com.cmpm.tv.data.TvNeonRepository
import com.cmpm.tv.data.LecturaFcDto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TvViewModel(
    private val context: Context
) : ViewModel() {

    private val neonRepo = TvNeonRepository()
    private val _state   = MutableStateFlow(TvUiState())
    val state: StateFlow<TvUiState> = _state.asStateFlow()

    init { cargarDatos() }

    fun cargarDatos() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading=true) }
            try {
                val lecturas = neonRepo.obtenerHistorialCompleto(50)
                val stats    = neonRepo.obtenerEstadisticas()
                _state.update { it.copy(
                    lecturas  = lecturas.map { it.toLecturaFC() },
                    estadisticas = stats.map { it.toLecturaFC() },
                    isLoading = false
                )}
            } catch (e: Exception) {
                _state.update { it.copy(error=e.message, isLoading=false) }
            }
        }
    }
    
    fun refresh() = cargarDatos()

    private fun LecturaFcDto.toLecturaFC(): LecturaFC {
        return LecturaFC(
            id = id,
            bpm = bpm,
            estado = estado,
            hora = hora,
            dispositivo = dispositivo
        )
    }
}

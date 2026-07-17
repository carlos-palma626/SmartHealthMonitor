// ui/viewmodel/DashboardViewModel.kt
package mx.utng.cmpm.smarthealthmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import mx.utng.cmpm.smarthealthmonitor.data.models.MockData
import mx.utng.cmpm.smarthealthmonitor.data.SmartHealthRepository
import mx.utng.cmpm.smarthealthmonitor.data.db.LecturaFC
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .map { if (it == 0) MockData.fcActual else it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MockData.fcActual
        )

    val pasos: StateFlow<Int> = SmartHealthRepository.pasosFlow
        .map { if (it == 0) MockData.pasosActual else it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MockData.pasosActual
        )

    // ← NUEVO: historial reactivo desde Room
    val historial: StateFlow<List<LecturaFC>> =
        SmartHealthRepository.obtenerHistorial()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
            
    fun simularLectura() {
        viewModelScope.launch {
            val randomBpm = (50..120).random()
            SmartHealthRepository.actualizarFC(randomBpm)
            // Simular actualización de pasos
            SmartHealthRepository.actualizarPasos(MockData.pasosActual + (10..50).random())
        }
    }

    fun simularLecturaWear() {
        viewModelScope.launch {
            val randomBpm = (50..120).random()
            SmartHealthRepository.actualizarFC(randomBpm, "wear")
        }
    }
    
    fun sincronizar() {
        viewModelScope.launch {
            SmartHealthRepository.sincronizar()
        }
    }
}
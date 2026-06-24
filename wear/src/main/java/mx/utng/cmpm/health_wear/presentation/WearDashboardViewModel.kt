package mx.utng.cmpm.health_wear.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import mx.utng.cmpm.smarthealthmonitor.data.SmartHealthRepository

class WearDashboardViewModel : ViewModel() {
   
    // Reutiliza el mismo Repository del módulo app
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .map { if (it == 0) 72 else it }  // valor por defecto
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 72
        )

    // Para el Reto Adicional: Conteo de pasos del día
    val pasos: StateFlow<Int> = SmartHealthRepository.pasosFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )
}

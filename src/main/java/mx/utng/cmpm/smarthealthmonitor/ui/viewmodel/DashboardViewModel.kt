// ui/viewmodel/DashboardViewModel.kt
package mx.utng.cmpm.smarthealthmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import mx.utng.cmpm.smarthealthmonitor.data.models.MockData
import mx.utng.cmpm.smarthealthmonitor.data.SmartHealthRepository

class DashboardViewModel : ViewModel() {

    // Frecuencia Cardíaca: viene del wearable real vía Repository.
    // Si es 0 (sin dato aún), usar el valor simulado de MockData.
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .map { if (it == 0) MockData.fcActual else it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MockData.fcActual
        )

    // Pasos: viene del wearable real vía Repository.
    // Si es 0 (sin dato aún), usar el valor simulado de MockData.
    val pasos: StateFlow<Int> = SmartHealthRepository.pasosFlow
        .map { if (it == 0) MockData.pasosActual else it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MockData.pasosActual
        )

    // Historial se queda con el MockData temporal por ahora
    val historial = MockData.historialFC
}
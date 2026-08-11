package mx.utng.cmpm.health_wear.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import mx.utng.cmpm.smarthealthmonitor.data.SmartHealthRepository
import mx.utng.cmpm.smarthealthmonitor.data.db.LecturaFC

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.launch
import mx.utng.cmpm.smarthealthmonitor.wear.mqtt.MqttWearPublisher

class WearDashboardViewModel(application: Application) : AndroidViewModel(application) {
   
    private val mqttPublisher = MqttWearPublisher(application)

    init {
        mqttPublisher.connect()
        viewModelScope.launch {
            SmartHealthRepository.fcFlow.collect { bpm ->
                val estado = when {
                    bpm < 60 -> "FC Baja"
                    bpm > 100 -> "FC Alta"
                    else -> "Normal"
                }
                mqttPublisher.publishFC(bpm, estado)
            }
        }
    }

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

    // Historial de lecturas de ritmo cardíaco
    val historial: StateFlow<List<LecturaFC>> = SmartHealthRepository.obtenerHistorial()

    override fun onCleared() {
        super.onCleared()
        mqttPublisher.disconnect()
    }
}

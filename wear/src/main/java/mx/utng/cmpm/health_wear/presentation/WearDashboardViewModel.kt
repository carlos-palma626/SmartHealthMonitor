package mx.utng.cmpm.health_wear.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import mx.utng.cmpm.smarthealthmonitor.data.SmartHealthRepository
import mx.utng.cmpm.smarthealthmonitor.data.db.LecturaFC
import kotlinx.coroutines.flow.MutableStateFlow

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.launch
import mx.utng.cmpm.smarthealthmonitor.wear.mqtt.MqttWearPublisher
import mx.utng.cmpm.smarthealthmonitor.wear.data.WearNeonRepository
import kotlinx.coroutines.Dispatchers

class WearDashboardViewModel(application: Application) : AndroidViewModel(application) {
   
    private val mqttPublisher = MqttWearPublisher(application)
    private val neonRepo = WearNeonRepository()

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
                
                // Publicar a Neon en IO thread
                launch(Dispatchers.IO) {
                    runCatching { neonRepo.publicarLectura(bpm, estado) }
                        .onFailure { android.util.Log.w("WEAR","Sin red: ${it.message}") }
                }
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

    // Historial de lecturas desde Neon
    private val _historialNeon = MutableStateFlow<List<LecturaFC>>(emptyList())
    val historial: StateFlow<List<LecturaFC>> = _historialNeon

    fun refreshHistorial() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { neonRepo.obtenerUltimasLecturas() }
                .onSuccess { dtoList ->
                    val lecturas = dtoList.map { dto ->
                        LecturaFC(
                            id = dto.id ?: 0,
                            bpm = dto.bpm ?: 0,
                            estado = dto.estado ?: "",
                            dispositivo = dto.dispositivo ?: "",
                            hora = dto.hora ?: ""
                        )
                    }
                    _historialNeon.value = lecturas
                }
                .onFailure {
                    android.util.Log.e("WEAR_DB", "Error al obtener historial: ${it.message}")
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mqttPublisher.disconnect()
    }
}

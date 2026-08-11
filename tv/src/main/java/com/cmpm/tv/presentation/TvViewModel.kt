package com.cmpm.tv.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.cmpm.tv.domain.model.TvUiState
import com.cmpm.tv.domain.repository.SmartHealthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.cmpm.smarthealthmonitor.tv.mqtt.MqttTvSubscriber
import mx.utng.cmpm.smarthealthmonitor.mqtt.TvMessage

class TvViewModel(
    private val repository: SmartHealthRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(TvUiState())
    val state: StateFlow<TvUiState> = _state.asStateFlow()

    // Flow de mensajes MQTT entrantes
    private val mqttFlow = MutableStateFlow<TvMessage?>(null)
    private val mqttSubscriber = MqttTvSubscriber(context, mqttFlow)

    init {
        mqttSubscriber.connect()

        // Observar mensajes MQTT y actualizar el estado de la UI
        viewModelScope.launch {
            mqttFlow.collect { tvMsg ->
                tvMsg ?: return@collect
                _state.update { it.copy(
                    fcActual = tvMsg.bpm,
                    fcEstado = tvMsg.estado,
                    ultimaHora = tvMsg.hora,
                    isLoading = false
                )}
            }
        }

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
                // Ya no actualizamos fcActual desde aquí, porque el origen de verdad ahora es MQTT.
                // _state.update { it.copy(fcActual = bpm) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mqttSubscriber.disconnect()
    }
}

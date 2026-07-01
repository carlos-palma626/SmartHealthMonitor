package mx.utng.cmpm.smarthealthmonitor.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mx.utng.cmpm.smarthealthmonitor.data.db.LecturaFC

object SmartHealthRepository {
    private val _fcFlow = MutableStateFlow(72)
    val fcFlow: StateFlow<Int> = _fcFlow

    private val _pasosFlow = MutableStateFlow(0)
    val pasosFlow: StateFlow<Int> = _pasosFlow

    private val _historial = MutableStateFlow<List<LecturaFC>>(emptyList())
    val historialFlow: StateFlow<List<LecturaFC>> = _historial

    fun updateHeartRate(fc: Int) {
        _fcFlow.value = fc
        // Add to history
        val list = _historial.value.toMutableList()
        list.add(0, LecturaFC(id = list.size + 1, valorBpm = fc))
        _historial.value = list.take(15) // Keep last 15 items
    }

    fun updateSteps(pasos: Int) {
        _pasosFlow.value = pasos
    }

    fun obtenerHistorial(): StateFlow<List<LecturaFC>> {
        return historialFlow
    }
}

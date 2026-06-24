package mx.utng.cmpm.smarthealthmonitor.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SmartHealthRepository {
    private val _fcFlow = MutableStateFlow(72)
    val fcFlow: StateFlow<Int> = _fcFlow

    private val _pasosFlow = MutableStateFlow(0)
    val pasosFlow: StateFlow<Int> = _pasosFlow

    fun updateHeartRate(fc: Int) {
        _fcFlow.value = fc
    }

    fun updateSteps(pasos: Int) {
        _pasosFlow.value = pasos
    }
}

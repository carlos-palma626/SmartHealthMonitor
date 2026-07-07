package com.cmpm.tv.domain.repository

import com.cmpm.tv.domain.model.LecturaFC
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface SmartHealthRepository {
    val fcActual: StateFlow<Int>
    fun obtenerHistorial(): Flow<List<LecturaFC>>
}

// A fake repository for the TV app to compile and run if it doesn't share room DB yet
class FakeSmartHealthRepository : SmartHealthRepository {
    override val fcActual = MutableStateFlow(72)
    override fun obtenerHistorial(): Flow<List<LecturaFC>> = kotlinx.coroutines.flow.flowOf(
        listOf(
            LecturaFC(bpm = 75, estado = "Normal", hora = "10:00"),
            LecturaFC(bpm = 80, estado = "Normal", hora = "10:05"),
            LecturaFC(bpm = 110, estado = "Anormal", hora = "10:10")
        )
    )
}

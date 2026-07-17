package mx.utng.cmpm.smarthealthmonitor.data.models

data class LecturaFC(
    val id: Int,
    val bpm: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val estado: String = if(bpm in 60..100) "Normal" else "Anormal"
)

// Datos de prueba para desarrollo (mock data)
object MockData {
    val historialFC = listOf(
        LecturaFC(2, 82, "10:30"),
        LecturaFC(3, 76, "10:00"),
        LecturaFC(4, 95, "09:30", false), // fuera de rango
        LecturaFC(5, 71, "09:00"),
        LecturaFC(6, 80, "08:30"),
        LecturaFC(7, 74, "08:00")
    )
    var fcActual = 78
    var pasosActual = 4250
}
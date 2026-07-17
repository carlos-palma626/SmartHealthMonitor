package mx.utng.cmpm.smarthealthmonitor.data.models

data class LecturaFC(
    val bpm: Int,
    val hora: String,
    val estado: String = if(bpm in 60..100) "Normal" else "Anormal"
)

// Datos de prueba para desarrollo (mock data)
object MockData {
    val historialFC = listOf(
        LecturaFC(bpm=78, estado="Normal", hora="11:00"),
        LecturaFC(bpm=82, estado="Normal", hora="10:30"),
        LecturaFC(bpm=76, estado="Normal", hora="10:00"),
        LecturaFC(bpm=95, estado="Normal", hora="09:30"),
        LecturaFC(bpm=71, estado="Normal", hora="09:00"),
        LecturaFC(bpm=80, estado="Normal", hora="08:30"),
        LecturaFC(bpm=74, estado="Normal", hora="08:00")
    )
    var fcActual = 78
    var pasosActual = 4250
}
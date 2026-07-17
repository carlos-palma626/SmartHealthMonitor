package mx.utng.cmpm.smarthealthmonitor.data.db

data class LecturaFC(
    val id: Int = 0,
    val bpm: Int,
    val estado: String,
    val dispositivo: String = "wear",
    val hora: String
)

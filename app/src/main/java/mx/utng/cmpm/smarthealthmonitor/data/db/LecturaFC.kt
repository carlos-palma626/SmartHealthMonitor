package mx.utng.cmpm.smarthealthmonitor.data.db

import androidx.room.*

@Entity(tableName = "lecturas_fc")
data class LecturaFC(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bpm: Int,
    val estado: String,
    val dispositivo: String = "app",
    val hora: String,
    @ColumnInfo(name = "sincronizado")
    val sincronizado: Boolean = false
)
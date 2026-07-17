package mx.utng.cmpm.smarthealthmonitor.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow

// Importaciones de tus modelos y base de datos
// Asegúrate de que las rutas coincidan con tu estructura de carpetas real
import mx.utng.cmpm.smarthealthmonitor.data.db.LecturaFC
import mx.utng.cmpm.smarthealthmonitor.data.db.LecturaFCDao
import mx.utng.cmpm.smarthealthmonitor.data.db.SmartHealthDB
import mx.utng.cmpm.smarthealthmonitor.data.repository.SyncRepository

// Importaciones de Java para manejo de fechas
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Importaciones de corrutinas para carga asíncrona
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

/**
 * Repositorio singleton que centraliza los datos de salud.
 */
object SmartHealthRepository {

    private val _fcFlow = MutableStateFlow(0)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    private val _pasosFlow = MutableStateFlow(0)
    val pasosFlow: StateFlow<Int> = _pasosFlow.asStateFlow()

    private var dao: LecturaFCDao? = null
    private var syncRepo: SyncRepository? = null
    private var sharedPreferences: android.content.SharedPreferences? = null

    fun init(context: Context) {
        dao = SmartHealthDB.getDatabase(context).lecturaDao()
        syncRepo = SyncRepository(dao!!)
        sharedPreferences = context.getSharedPreferences("smart_health_prefs", Context.MODE_PRIVATE)

        // Cargar los últimos pasos guardados
        _pasosFlow.value = sharedPreferences?.getInt("key_pasos", 0) ?: 0

        // Cargar la última frecuencia cardíaca registrada de forma asíncrona o pre-poblar si está vacía
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Pre-poblar
                val countFlow = dao?.contarPendientes()?.firstOrNull() ?: 0
                // We'll skip pre-populating since contarRegistros was removed, or we can just fetch all
                val all = dao?.obtenerTodas()?.firstOrNull()
                if (all.isNullOrEmpty()) {
                    syncRepo?.insertarLectura(LecturaFC(bpm = 75, estado = "Normal", hora = "11:30"))
                    syncRepo?.insertarLectura(LecturaFC(bpm = 95, estado = "Normal", hora = "11:45"))
                    syncRepo?.insertarLectura(LecturaFC(bpm = 110, estado = "FC Alta", hora = "12:00"))
                }
                dao?.obtenerTodas()?.firstOrNull()?.firstOrNull()?.let { last ->
                    _fcFlow.value = last.bpm
                }
            } catch (e: Exception) {
                // Manejar excepción silenciosamente
            }
        }
    }

    suspend fun actualizarFC(bpm: Int, dispositivoOrigen: String = "app") {
        _fcFlow.value = bpm

        // Crear instancia de la fecha actual
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val estado = when {
            bpm < 60 -> "FC Baja"
            bpm > 100 -> "FC Alta"
            else -> "Normal"
        }

        // Persistir en Room (id 0 para autoincremento) y sincronizar
        val nuevaLectura = LecturaFC(
            id = 0,
            bpm = bpm,
            estado = estado,
            dispositivo = dispositivoOrigen,
            hora = timestamp
        )

        syncRepo?.insertarLectura(nuevaLectura)
    }

    fun actualizarPasos(pasos: Int) {
        _pasosFlow.value = pasos
        sharedPreferences?.edit()?.putInt("key_pasos", pasos)?.apply()
    }

    fun obtenerHistorial(): Flow<List<LecturaFC>> =
        syncRepo?.observarHistorial() ?: emptyFlow()
        
    suspend fun sincronizar() {
        syncRepo?.sincronizarDesdeNeon()
    }
}
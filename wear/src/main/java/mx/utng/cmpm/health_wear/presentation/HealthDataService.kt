package mx.utng.cmpm.health_wear.presentation

import android.content.Context
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.*
import androidx.health.services.client.HealthServices
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.*
import kotlinx.coroutines.guava.await

class HealthDataService : PassiveListenerService() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        val fcDataPoints = dataPoints.getData(DataType.HEART_RATE_BPM)
        fcDataPoints.forEach { dataPoint ->
            if (dataPoint is SampleDataPoint<*>) {
                try {
                    // 🔹 EXTRACCIÓN SEGURA: Convierte a string y luego a número para evitar fallas de casteo entre Long/Double
                    val bpm = dataPoint.value.toString().toDouble().toInt()
                    mx.utng.cmpm.smarthealthmonitor.data.SmartHealthRepository.updateHeartRate(bpm)
                    android.util.Log.d("HealthDataService", "--- HEALTH SERVICES DATA: Ritmo cardíaco a $bpm bpm ---")

                    val messageClient = Wearable.getMessageClient(applicationContext)
                    Wearable.getNodeClient(applicationContext).connectedNodes
                        .addOnSuccessListener { nodes ->
                            android.util.Log.d("HealthDataService", "Nodos conectados encontrados: ${nodes.size}")
                            for (node in nodes) {
                                android.util.Log.d("HealthDataService", "Enviando mensaje a nodo: ${node.displayName}")
                                messageClient.sendMessage(node.id, "/smarthealthmonitor/fc", bpm.toString().toByteArray())
                                    .addOnSuccessListener {
                                        android.util.Log.d("HealthDataService", "Mensaje enviado con éxito a ${node.displayName}")
                                    }
                                    .addOnFailureListener { e ->
                                        android.util.Log.e("HealthDataService", "Falla al enviar a ${node.displayName}", e)
                                    }
                            }
                            if (nodes.isEmpty()) {
                                android.util.Log.w("HealthDataService", "No se encontraron nodos móviles conectados. ¿Están emparejados los emuladores?")
                            }
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("HealthDataService", "Error al obtener nodos conectados", e)
                        }
                } catch (e: Exception) {
                    // Captura fallas de lectura sin tirar la app
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        suspend fun registrar(context: Context) {
            val hsClient = HealthServices.getClient(context)
            val passiveClient = hsClient.passiveMonitoringClient

            val config = PassiveListenerConfig.builder()
                .setDataTypes(setOf(DataType.HEART_RATE_BPM))
                .setShouldUserActivityInfoBeRequested(true)
                .build()

            passiveClient.setPassiveListenerServiceAsync(
                HealthDataService::class.java,
                config
            ).await()
        }
    }
}
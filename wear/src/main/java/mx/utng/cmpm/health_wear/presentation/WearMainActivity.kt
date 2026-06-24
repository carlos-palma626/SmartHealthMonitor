package mx.utng.cmpm.health_wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import kotlinx.coroutines.launch
import mx.utng.cmpm.health_wear.R
import mx.utng.cmpm.health_wear.presentation.theme.SmartHealthMonitorTheme
import mx.utng.cmpm.health_wear.presentation.theme.SmartHealthWearTheme
import mx.utng.cmpm.health_wear.presentation.SmartHealthWearNavGraph

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.google.android.gms.wearable.Wearable

class WearMainActivity : ComponentActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var heartRateSensor: Sensor? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val bodySensorsGranted = permissions[Manifest.permission.BODY_SENSORS] ?: false
        val activityGranted = permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false

        if (bodySensorsGranted && activityGranted) {
            inicializarHealthServices()
        } else {
            Toast.makeText(this, "Se requieren todos los permisos para usar el sensor", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        android.util.Log.d("WEAR_DEBUG", "=== onCreate: iniciando app ===")

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        if (heartRateSensor == null) {
            android.util.Log.e("WEAR_DEBUG", "=== SENSOR NO ENCONTRADO en este dispositivo ===")
        } else {
            android.util.Log.d("WEAR_DEBUG", "=== Sensor de ritmo cardíaco encontrado OK ===")
        }

        if (tienePermisos()) {
            android.util.Log.d("WEAR_DEBUG", "=== Permisos OK, inicializando... ===")
            inicializarHealthServices()
        } else {
            android.util.Log.w("WEAR_DEBUG", "=== Faltan permisos, pidiendo... ===")
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BODY_SENSORS,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            )
        }

        setContent {
            SmartHealthWearTheme {
                SmartHealthWearNavGraph()
            }
        }
    }

    private fun tienePermisos(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
    }

    private fun inicializarHealthServices() {
        android.util.Log.d("WEAR_DEBUG", "=== inicializarHealthServices() llamado ===")
        lifecycleScope.launch {
            try {
                HealthDataService.registrar(applicationContext)
                android.util.Log.d("WEAR_DEBUG", "=== HealthDataService registrado con ÉXITO ===")
            } catch (e: Exception) {
                android.util.Log.e("WEAR_DEBUG", "=== ERROR registrando HealthDataService ===", e)
            }
        }

        val resultado = sensorManager?.registerListener(
            this,
            heartRateSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        android.util.Log.d("WEAR_DEBUG", "=== registerListener resultado: $resultado (true=OK, null=sensor es null) ===")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        android.util.Log.d("WEAR_DEBUG", "=== onSensorChanged disparado, tipo: ${event?.sensor?.type} ===")

        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            val bpm = event.values[0].toInt()
            mx.utng.cmpm.smarthealthmonitor.data.SmartHealthRepository.updateHeartRate(bpm)
            android.util.Log.d("WEAR_DEBUG", "=== BPM CAPTURADO: $bpm — buscando nodos para enviar... ===")

            val messageClient = Wearable.getMessageClient(applicationContext)
            Wearable.getNodeClient(applicationContext).connectedNodes
                .addOnSuccessListener { nodes ->
                    android.util.Log.d("WEAR_DEBUG", "=== Nodos encontrados: ${nodes.size} ===")
                    if (nodes.isEmpty()) {
                        android.util.Log.w("WEAR_DEBUG", "=== SIN NODOS — el teléfono no está conectado al reloj ===")
                    }
                    for (node in nodes) {
                        android.util.Log.d("WEAR_DEBUG", "=== Enviando $bpm bpm a nodo: ${node.displayName} (${node.id}) ===")
                        messageClient.sendMessage(
                            node.id,
                            "/smarthealthmonitor/fc",
                            bpm.toString().toByteArray()
                        )
                            .addOnSuccessListener {
                                android.util.Log.d("WEAR_DEBUG", "=== ✅ MENSAJE ENVIADO CON ÉXITO a ${node.displayName} ===")
                            }
                            .addOnFailureListener { e ->
                                android.util.Log.e("WEAR_DEBUG", "=== ❌ FALLÓ envío a ${node.displayName} ===", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("WEAR_DEBUG", "=== ❌ ERROR obteniendo nodos ===", e)
                }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        android.util.Log.d("WEAR_DEBUG", "=== onAccuracyChanged: accuracy=$accuracy ===")
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
    }
}

@Composable
fun WearApp(greetingName: String) {
    SmartHealthMonitorTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState,
                edgeButton = {
                    EdgeButton(
                        onClick = { /*TODO*/ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        Text("More")
                    }
                },
            ) { contentPadding ->
                TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                    item {
                        ListHeader(
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = stringResource(R.string.hello_world, greetingName))
                        }
                    }
                    item {
                        Button(
                            onClick = { /*TODO*/ },
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("Button A")
                        }
                    }
                }
            }
        }
    }
}
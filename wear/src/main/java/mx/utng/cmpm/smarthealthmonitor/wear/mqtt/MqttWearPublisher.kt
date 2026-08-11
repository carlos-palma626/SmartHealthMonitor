package mx.utng.cmpm.smarthealthmonitor.wear.mqtt

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mx.utng.cmpm.smarthealthmonitor.mqtt.MqttConfig
import mx.utng.cmpm.smarthealthmonitor.mqtt.FcMessage
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttWearPublisher(private val context: Context) {

    private var client: MqttAsyncClient? = null

    fun connect() {
        if (MqttConfig.BROKER_URL.isEmpty() || MqttConfig.BROKER_URL == "\"\"") {
            android.util.Log.e("MQTT_WEAR", "❌ URL de broker vacía. Revisa local.properties")
            return
        }

        try {
            client = MqttAsyncClient(
                MqttConfig.BROKER_URL,
                MqttConfig.CLIENT_WEAR,
                MemoryPersistence()
            )

            val options = MqttConnectOptions().apply {
                userName        = MqttConfig.USERNAME
                password        = MqttConfig.PASSWORD.toCharArray()
                isCleanSession  = true
                connectionTimeout = 30
                keepAliveInterval = 60
                // SSL habilitado automáticamente por la URL ssl://
                socketFactory = javax.net.ssl.SSLSocketFactory.getDefault()
            }

            client?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    android.util.Log.d("MQTT_WEAR", "✅ Conectado a HiveMQ Cloud")
                }
                override fun onFailure(token: IMqttToken?, ex: Throwable?) {
                    android.util.Log.e("MQTT_WEAR", "❌ Error de conexión: ${ex?.message}")
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("MQTT_WEAR", "❌ Exception al conectar: ${e.message}")
        }
    }

    /** Publicar FC al topic MQTT */
    fun publishFC(bpm: Int, estado: String) {
        if (client?.isConnected != true) return

        try {
            val message = FcMessage(bpm = bpm, estado = estado)
            val payload = Json.encodeToString(message).toByteArray()

            val mqttMessage = MqttMessage(payload).apply {
                qos      = MqttConfig.QOS
                isRetained = true  // el TV verá el último valor al conectarse
            }

            client?.publish(MqttConfig.TOPIC_FC, mqttMessage)
            android.util.Log.d("MQTT_WEAR", "📤 Publicado: $bpm bpm → ${MqttConfig.TOPIC_FC}")
        } catch (e: Exception) {
            android.util.Log.e("MQTT_WEAR", "❌ Error al publicar: ${e.message}")
        }
    }

    fun disconnect() { 
        try {
            client?.disconnect()
        } catch(e: Exception) { }
    }
}

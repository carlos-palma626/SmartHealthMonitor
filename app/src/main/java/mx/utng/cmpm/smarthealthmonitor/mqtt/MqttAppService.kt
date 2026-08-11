package mx.utng.cmpm.smarthealthmonitor.mqtt

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.utng.cmpm.smarthealthmonitor.data.SmartHealthRepository

class MqttAppService(
    private val context  : Context
) {
    private var client: MqttAsyncClient? = null

    fun connect() {
        if (MqttConfig.BROKER_URL.isEmpty() || MqttConfig.BROKER_URL == "\"\"") {
            android.util.Log.e("MQTT_APP", "❌ URL de broker vacía. Revisa local.properties")
            return
        }

        try {
            client = MqttAsyncClient(
                MqttConfig.BROKER_URL,
                MqttConfig.CLIENT_APP, 
                MemoryPersistence()
            )

            val options = MqttConnectOptions().apply {
                userName = MqttConfig.USERNAME
                password = MqttConfig.PASSWORD.toCharArray()
                isCleanSession = true
                socketFactory = javax.net.ssl.SSLSocketFactory.getDefault()
            }

            client?.setCallback(object : MqttCallback {
                override fun messageArrived(topic: String, msg: MqttMessage) {
                    when (topic) {
                        MqttConfig.TOPIC_FC -> handleFcMessage(msg)
                    }
                }
                override fun connectionLost(cause: Throwable?) {
                    android.util.Log.w("MQTT_APP","Conexión perdida: ${cause?.message}")
                }
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            client?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(token: IMqttToken?) {
                    client?.subscribe(MqttConfig.TOPIC_FC, MqttConfig.QOS)
                    android.util.Log.d("MQTT_APP","✅ Conectado y suscrito a ${MqttConfig.TOPIC_FC}")
                }
                override fun onFailure(token: IMqttToken?, ex: Throwable?) {
                    android.util.Log.e("MQTT_APP","❌ Error de conexión: ${ex?.message}")
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("MQTT_APP", "❌ Exception al conectar: ${e.message}")
        }
    }

    private fun handleFcMessage(msg: MqttMessage) {
        try {
            val fcMsg = Json.decodeFromString<FcMessage>(String(msg.payload))

            CoroutineScope(Dispatchers.IO).launch {
                SmartHealthRepository.actualizarFC(fcMsg.bpm)
            }

            val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val tvMsg = TvMessage(bpm = fcMsg.bpm, estado = fcMsg.estado, hora = hora)
            val tvPayload = Json.encodeToString(tvMsg).toByteArray()
            
            val tvMqtt = MqttMessage(tvPayload).apply {
                qos = MqttConfig.QOS; isRetained = true
            }
            client?.publish(MqttConfig.TOPIC_TV, tvMqtt)
            android.util.Log.d("MQTT_APP","🔁 Re-publicado al TV: ${fcMsg.bpm} bpm")
        } catch (e: Exception) {
            android.util.Log.e("MQTT_APP", "❌ Error al manejar mensaje: ${e.message}")
        }
    }

    fun publicarHaciaTv(bpm: Int, estado: String) {
        try {
            val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val tvMsg = TvMessage(bpm = bpm, estado = estado, hora = hora)
            val tvPayload = Json.encodeToString(tvMsg).toByteArray()
            
            val tvMqtt = MqttMessage(tvPayload).apply {
                qos = MqttConfig.QOS; isRetained = true
            }
            client?.publish(MqttConfig.TOPIC_TV, tvMqtt)
            android.util.Log.d("MQTT_APP","🔁 Publicado MANUAL al TV: $bpm bpm")
        } catch (e: Exception) {
            android.util.Log.e("MQTT_APP", "❌ Error al simular y publicar: ${e.message}")
        }
    }

    fun disconnect() { 
        try { client?.disconnect() } catch (e: Exception) { }
    }
}

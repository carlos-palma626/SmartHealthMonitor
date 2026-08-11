package mx.utng.cmpm.smarthealthmonitor

import android.app.Application
import mx.utng.cmpm.smarthealthmonitor.data.SmartHealthRepository
import mx.utng.cmpm.smarthealthmonitor.mqtt.MqttAppService

class SmartHealthApp : Application() {
    lateinit var mqttService: MqttAppService

    override fun onCreate() {
        super.onCreate()
        SmartHealthRepository.init(this)  // inicializar Room
        
        mqttService = MqttAppService(this)
        mqttService.connect()
    }
}

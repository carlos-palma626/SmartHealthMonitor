package mx.utng.cmpm.smarthealthmonitor

import android.app.Application
import mx.utng.cmpm.smarthealthmonitor.data.SmartHealthRepository

class SmartHealthApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SmartHealthRepository.init(this)  // inicializar Room
    }
}

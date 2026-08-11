package mx.utng.cmpm.smarthealthmonitor.mqtt

import com.cmpm.tv.BuildConfig

object MqttConfig {
    const val BROKER_URL  = BuildConfig.HIVEMQ_BROKER_URL
    const val USERNAME    = BuildConfig.HIVEMQ_USERNAME
    const val PASSWORD    = BuildConfig.HIVEMQ_PASSWORD
    const val TOPIC_FC    = "utng/smarthealthmonitor/fc"
    const val TOPIC_TV    = "utng/smarthealthmonitor/tv"
    const val TOPIC_ALERT = "utng/smarthealthmonitor/alerta"
    const val QOS = 1
    const val CLIENT_WEAR = "smarthealthmonitor-wear"
    const val CLIENT_APP  = "smarthealthmonitor-app"
    const val CLIENT_TV   = "smarthealthmonitor-tv"
}

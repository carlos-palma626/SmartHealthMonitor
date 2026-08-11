package mx.utng.cmpm.smarthealthmonitor.mqtt

import mx.utng.cmpm.smarthealthmonitor.BuildConfig

object MqttConfig {
    // Leemos las credenciales desde BuildConfig (que a su vez las toma de local.properties)
    const val BROKER_URL  = BuildConfig.HIVEMQ_BROKER_URL
    const val USERNAME    = BuildConfig.HIVEMQ_USERNAME
    const val PASSWORD    = BuildConfig.HIVEMQ_PASSWORD

    // Topics del proyecto
    const val TOPIC_FC    = "utng/smarthealthmonitor/fc"
    const val TOPIC_TV    = "utng/smarthealthmonitor/tv"
    const val TOPIC_ALERT = "utng/smarthealthmonitor/alerta"

    // QoS: 0=best effort, 1=at least once, 2=exactly once
    const val QOS = 1

    // Client IDs únicos por dispositivo
    const val CLIENT_WEAR = "smarthealthmonitor-wear"
    const val CLIENT_APP  = "smarthealthmonitor-app"
    const val CLIENT_TV   = "smarthealthmonitor-tv"
}

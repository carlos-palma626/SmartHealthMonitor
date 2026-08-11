

| 🔌  COMUNICACIÓN MQTT ENTRE DISPOSITIVOS Wear OS  →  Teléfono  →  Android TV *HiveMQ Cloud · Eclipse Paho MQTT · Jetpack Compose · UTNG 2025* ⌚  Wear OS  publishes FC → utng/smarthealthmonitor/fc ☁️  HiveMQ Cloud  (broker MQTT) 📱  Teléfono  subscribes & republishes → utng/smarthealthmonitor/tv 📺  Android TV  subscribes & renders FC en Compose for TV  |
| ----- |

| Alumno(s): |  | Grupo: |  |
| :---- | :---- | :---- | ----- |
| **Repositorio:** | github.com/\[usuario\]/SmartHealthMonitor |  |  |

| ¿Por qué MQTT y no Wearable Data Layer? La Wearable Data Layer API requiere que el teléfono y el reloj estén emparejados por Bluetooth. En el emulador, este emparejamiento es complejo de configurar. MQTT usa la red WiFi/internet del emulador directamente — los tres dispositivos se comunican sin emparejamiento BLE, usando solo el broker en la nube. |
| :---- |

# **1\. Arquitectura del sistema MQTT**

MQTT (Message Queuing Telemetry Transport) es un protocolo de mensajería ligero basado en el patrón Publicador–Suscriptor. Un broker central recibe los mensajes de los publicadores y los distribuye a los suscriptores. Cada mensaje se publica en un 'topic' (ruta) que funciona como canal.

| Dispositivo | Rol MQTT | Topic que usa | Acción |
| :---- | :---- | :---- | :---- |
| **⌚ Wear OS** | Publicador | utng/smarthealthmonitor/fc | Publica {"bpm":72, "estado":"Normal"} cada vez que cambia la FC |
| **📱 Teléfono** | Suscriptor \+ Publicador | utng/smarthealthmonitor/fcutng/smarthealthmonitor/tv | Recibe la FC del reloj, actualiza la UI y la reenvía al topic TV |
| **📺 Android TV** | Suscriptor | utng/smarthealthmonitor/tv | Recibe el JSON y actualiza TvCatalogScreen en tiempo real |
| **☁️ HiveMQ Cloud** | Broker | Todos | Retransmite mensajes entre publicadores y suscriptores |

**Topics del proyecto (convención UTNG):**

| Topic | Payload JSON |
| :---- | :---- |
| utng/smarthealthmonitor/fc | {"bpm": 72, "estado": "Normal", "timestamp": 1700000000} |
| utng/smarthealthmonitor/tv | {"bpm": 72, "estado": "Normal", "hora": "10:30:00"} |
| utng/smarthealthmonitor/alerta | {"tipo": "FC\_ALTA", "bpm": 135, "mensaje": "FC fuera de rango"} |

# **2\. Configurar HiveMQ Cloud (5 min)**

| PASO A  Crear cuenta y cluster en HiveMQ Cloud |
| :---- |
| Ir a https://www.hivemq.com/mqtt-cloud-broker/ y registrarse (plan Free Tier). Crear un nuevo cluster. HiveMQ genera automáticamente: **Dato** **Ejemplo (el tuyo será diferente)** **Broker URL** abc123def456.s1.eu.hivemq.cloud **Puerto TLS** 8883  (MQTT sobre SSL — OBLIGATORIO en HiveMQ Cloud) **Usuario** tu-usuario-hivemq **Contraseña** tu-contraseña-segura En el cluster → Access Management → crear credenciales de acceso. Guardar los datos en un archivo local. NO los pongas directamente en el código — úsalos como constantes en un objeto MqttConfig.kt. **Seguridad: no subir las credenciales al repositorio. Agregar un archivo local.properties con las credenciales y leerlas desde BuildConfig. Agregar local.properties al .gitignore.**  |

| PASO B  Verificar conexión con MQTT Explorer (herramienta de escritorio) |
| :---- |
| Antes de codificar, verifica que el broker funciona con una herramienta visual: Descargar MQTT Explorer desde http://mqtt-explorer.com Nueva conexión: Host \= tu-cluster.hivemq.cloud, Puerto \= 8883, TLS activado, usuario \+ contraseña. Conectar y publicar manualmente en el topic utng/smarthealthmonitor/fc con el payload: {"bpm":72} Verificar que el mensaje aparece en el árbol de topics. Si aparece, el broker está funcionando. **Este paso evita el 90% de los problemas de conexión. Si MQTT Explorer no conecta, el código tampoco conectará.**  |

# **3\. Dependencias y Configuración Compartida**

| PASO 1  Agregar Eclipse Paho MQTT a los 3 módulos |
| :---- |
| Eclipse Paho es la librería MQTT oficial para Android. Se agrega a los módulos wear, app y tv: **📄  settings.gradle.kts — agregar repositorio JitPack** dependencyResolutionManagement {     repositories {         google()         mavenCentral()         maven { url \= uri("https://repo.eclipse.org/content/repositories/paho-releases/") }     } } **📄  wear/build.gradle.kts  \+  app/build.gradle.kts  \+  tv/build.gradle.kts** dependencies {     // Eclipse Paho MQTT para Android     implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")     implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")     // Kotlinx Serialization para JSON     implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3") } **📄  app/build.gradle.kts — habilitar serialización** plugins {     // Agregar junto a los otros plugins     id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" } *Commit: feat(mqtt): add Eclipse Paho MQTT and Kotlinx Serialization to all modules*  |

| PASO 2  MqttConfig.kt y MqttMessage.kt — Configuración compartida |
| :---- |
| Crea estos archivos en el módulo app (shared). Los módulos wear y tv los referenciarán: **📄  app/src/main/java/.../mqtt/MqttConfig.kt** package mx.utng.smarthealthmonitor.mqtt   object MqttConfig {     // ⚠️ Reemplaza con los datos de TU cluster HiveMQ     const val BROKER\_URL  \= "ssl://TU-CLUSTER.hivemq.cloud:8883"     const val USERNAME    \= "TU-USUARIO-HIVEMQ"  // del Access Management     const val PASSWORD    \= "TU-CONTRASEÑA"       // Topics del proyecto     const val TOPIC\_FC    \= "utng/smarthealthmonitor/fc"     const val TOPIC\_TV    \= "utng/smarthealthmonitor/tv"     const val TOPIC\_ALERT \= "utng/smarthealthmonitor/alerta"       // QoS: 0=best effort, 1=at least once, 2=exactly once     const val QOS \= 1       // Client IDs únicos por dispositivo     const val CLIENT\_WEAR \= "smarthealthmonitor-wear"     const val CLIENT\_APP  \= "smarthealthmonitor-app"     const val CLIENT\_TV   \= "smarthealthmonitor-tv" } **📄  app/src/main/java/.../mqtt/MqttMessage.kt — Modelos de mensajes** package mx.utng.smarthealthmonitor.mqtt import kotlinx.serialization.Serializable   @Serializable data class FcMessage(     val bpm       : Int,     val estado    : String,     val timestamp : Long \= System.currentTimeMillis() )   @Serializable data class TvMessage(     val bpm    : Int,     val estado : String,     val hora   : String )   @Serializable data class AlertMessage(     val tipo    : String,     val bpm     : Int,     val mensaje : String ) *Commit: feat(mqtt): add MqttConfig and message data classes*  |

# **4\. Wear OS — Publicador MQTT de FC**

| PASO 3  MqttWearPublisher.kt — Publicar FC desde el reloj |
| :---- |
| El reloj publica la FC cada vez que el sensor detecta un nuevo valor. La conexión usa SSL (puerto 8883): **📄  wear/src/main/java/.../mqtt/MqttWearPublisher.kt** package mx.utng.smarthealthmonitor.wear.mqtt import android.content.Context import kotlinx.coroutines.\* import kotlinx.serialization.encodeToString import kotlinx.serialization.json.Json import mx.utng.smarthealthmonitor.mqtt.MqttConfig import mx.utng.smarthealthmonitor.mqtt.FcMessage import org.eclipse.paho.client.mqttv3.\* import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence   class MqttWearPublisher(private val context: Context) {       private var client: MqttAsyncClient? \= null       fun connect() {         client \= MqttAsyncClient(             MqttConfig.BROKER\_URL,             MqttConfig.CLIENT\_WEAR,             MemoryPersistence()         )           val options \= MqttConnectOptions().apply {             userName        \= MqttConfig.USERNAME             password        \= MqttConfig.PASSWORD.toCharArray()             isCleanSession  \= true             connectionTimeout \= 30             keepAliveInterval \= 60             // SSL habilitado automáticamente por la URL ssl://             socketFactory \= javax.net.ssl.SSLSocketFactory.getDefault()         }           client?.connect(options, null, object : IMqttActionListener {             override fun onSuccess(asyncActionToken: IMqttToken?) {                 android.util.Log.d("MQTT\_WEAR", "✅ Conectado a HiveMQ Cloud")             }             override fun onFailure(token: IMqttToken?, ex: Throwable?) {                 android.util.Log.e("MQTT\_WEAR", "❌ Error: ${ex?.message}")             }         })     }       /\*\* Publicar FC al topic MQTT \*/     fun publishFC(bpm: Int, estado: String) {         if (client?.isConnected \!= true) return           val message \= FcMessage(bpm \= bpm, estado \= estado)         val payload \= Json.encodeToString(message).toByteArray()           val mqttMessage \= MqttMessage(payload).apply {             qos      \= MqttConfig.QOS             isRetained \= true  // el TV verá el último valor al conectarse         }           client?.publish(MqttConfig.TOPIC\_FC, mqttMessage)         android.util.Log.d("MQTT\_WEAR", "📤 Publicado: ${bpm} bpm → ${MqttConfig.TOPIC\_FC}")     }       fun disconnect() { client?.disconnect() } } **Integrar en el WearViewModel — publicar cada vez que llega una nueva lectura:** **📄  wear/presentation/WearViewModel.kt — agregar publicación MQTT** // En el init{} del WearViewModel, después de startMonitoring(): private val mqttPublisher \= MqttWearPublisher(application)   init {     mqttPublisher.connect()     viewModelScope.launch {         heartRateSource.heartRate.collect { bpm \-\>             \_state.update { it.copy(fcActual \= bpm) }             // Publicar FC vía MQTT cada vez que cambia             val estado \= when { bpm \< 60 \-\> "FC Baja"; bpm \> 100 \-\> "FC Alta"; else \-\> "Normal" }             mqttPublisher.publishFC(bpm, estado)         }     } }   override fun onCleared() {     super.onCleared()     mqttPublisher.disconnect() } *Commit: feat(wear/mqtt): add MqttWearPublisher — publishes FC to HiveMQ Cloud on every sensor update*  |

# **5\. Teléfono — Suscriptor y Re-publicador**

| PASO 4  MqttAppService.kt — Suscribir y re-publicar al topic TV |
| :---- |
| El teléfono recibe los mensajes del reloj, actualiza el Repository y los reenvía al topic TV para que la pantalla inteligente los reciba: **📄  app/src/main/java/.../mqtt/MqttAppService.kt** package mx.utng.smarthealthmonitor.mqtt import android.content.Context import kotlinx.coroutines.flow.MutableStateFlow import kotlinx.serialization.encodeToString import kotlinx.serialization.json.Json import org.eclipse.paho.client.mqttv3.\* import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence import java.text.SimpleDateFormat import java.util.Date   class MqttAppService(     private val context  : Context,     private val fcFlow   : MutableStateFlow\<Int\>   // actualiza el Repository ) {     private var client: MqttAsyncClient? \= null       fun connect() {         client \= MqttAsyncClient(MqttConfig.BROKER\_URL,                                  MqttConfig.CLIENT\_APP, MemoryPersistence())           val options \= MqttConnectOptions().apply {             userName \= MqttConfig.USERNAME             password \= MqttConfig.PASSWORD.toCharArray()             isCleanSession \= true             socketFactory \= javax.net.ssl.SSLSocketFactory.getDefault()         }           // Callback de mensajes entrantes         client?.setCallback(object : MqttCallback {             override fun messageArrived(topic: String, msg: MqttMessage) {                 when (topic) {                     MqttConfig.TOPIC\_FC \-\> handleFcMessage(msg)                 }             }             override fun connectionLost(cause: Throwable?) {                 android.util.Log.w("MQTT\_APP","Conexión perdida: ${cause?.message}")             }             override fun deliveryComplete(token: IMqttDeliveryToken?) {}         })           client?.connect(options, null, object : IMqttActionListener {             override fun onSuccess(token: IMqttToken?) {                 // Suscribirse al topic de FC del reloj                 client?.subscribe(MqttConfig.TOPIC\_FC, MqttConfig.QOS)                 android.util.Log.d("MQTT\_APP","✅ Conectado y suscrito a ${MqttConfig.TOPIC\_FC}")             }             override fun onFailure(token: IMqttToken?, ex: Throwable?) {                 android.util.Log.e("MQTT\_APP","❌ Error: ${ex?.message}")             }         })     }       private fun handleFcMessage(msg: MqttMessage) {         val fcMsg \= Json.decodeFromString\<FcMessage\>(String(msg.payload))           // 1\. Actualizar el StateFlow del Repository         fcFlow.value \= fcMsg.bpm           // 2\. Re-publicar al topic TV con formato enriquecido         val hora \= SimpleDateFormat("HH:mm:ss").format(Date())         val tvMsg \= TvMessage(bpm=fcMsg.bpm, estado=fcMsg.estado, hora=hora)         val tvPayload \= Json.encodeToString(tvMsg).toByteArray()         val tvMqtt \= MqttMessage(tvPayload).apply {             qos \= MqttConfig.QOS; isRetained \= true         }         client?.publish(MqttConfig.TOPIC\_TV, tvMqtt)         android.util.Log.d("MQTT\_APP","🔁 Re-publicado al TV: ${fcMsg.bpm} bpm")     }       fun disconnect() { client?.disconnect() } } **Inicializar en el Application o en SmartHealthRepository:** **📄  app/SmartHealthApplication.kt** class SmartHealthApplication : Application() {     lateinit var mqttService: MqttAppService       override fun onCreate() {         super.onCreate()         // Inicializar MQTT con el StateFlow del Repository         mqttService \= MqttAppService(             context \= this,             fcFlow  \= SmartHealthRepository.fcFlow         )         mqttService.connect()     } } *Commit: feat(app/mqtt): add MqttAppService — subscribes to FC topic and re-publishes to TV topic*  |

# **6\. Android TV — Suscriptor MQTT**

| PASO 5  MqttTvSubscriber.kt \+ TvViewModel actualizado |
| :---- |
| La TV se suscribe al topic TV y actualiza el TvUiState cada vez que llega un mensaje: **📄  tv/src/main/java/.../mqtt/MqttTvSubscriber.kt** package mx.utng.smarthealthmonitor.tv.mqtt import android.content.Context import kotlinx.coroutines.flow.MutableStateFlow import kotlinx.serialization.json.Json import mx.utng.smarthealthmonitor.mqtt.\* import org.eclipse.paho.client.mqttv3.\* import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence   class MqttTvSubscriber(     private val context : Context,     private val tvFlow  : MutableStateFlow\<TvMessage?\>   // actualiza el ViewModel ) {     private var client: MqttAsyncClient? \= null       fun connect() {         client \= MqttAsyncClient(MqttConfig.BROKER\_URL,                                  MqttConfig.CLIENT\_TV, MemoryPersistence())           client?.setCallback(object : MqttCallback {             override fun messageArrived(topic: String, msg: MqttMessage) {                 if (topic \== MqttConfig.TOPIC\_TV) {                     val tvMsg \= Json.decodeFromString\<TvMessage\>(String(msg.payload))                     tvFlow.value \= tvMsg                     android.util.Log.d("MQTT\_TV","📺 Recibido: ${tvMsg.bpm} bpm")                 }             }             override fun connectionLost(cause: Throwable?) {}             override fun deliveryComplete(token: IMqttDeliveryToken?) {}         })           val options \= MqttConnectOptions().apply {             userName \= MqttConfig.USERNAME             password \= MqttConfig.PASSWORD.toCharArray()             isCleanSession \= true             socketFactory \= javax.net.ssl.SSLSocketFactory.getDefault()         }           client?.connect(options, null, object : IMqttActionListener {             override fun onSuccess(token: IMqttToken?) {                 client?.subscribe(MqttConfig.TOPIC\_TV, MqttConfig.QOS)                 android.util.Log.d("MQTT\_TV","✅ TV suscrita a ${MqttConfig.TOPIC\_TV}")             }             override fun onFailure(token: IMqttToken?, ex: Throwable?) {                 android.util.Log.e("MQTT\_TV","❌ Error: ${ex?.message}")             }         })     }     fun disconnect() { client?.disconnect() } } **Actualizar TvViewModel para observar los mensajes MQTT:** **📄  tv/presentation/TvViewModel.kt — agregar MQTT** class TvViewModel(     private val repository : SmartHealthRepository,     private val context    : Context ) : ViewModel() {       private val \_state \= MutableStateFlow(TvUiState())     val state: StateFlow\<TvUiState\> \= \_state.asStateFlow()       // Flow de mensajes MQTT entrantes     private val mqttFlow \= MutableStateFlow\<TvMessage?\>(null)     private val mqttSubscriber \= MqttTvSubscriber(context, mqttFlow)       init {         mqttSubscriber.connect()           // Observar mensajes MQTT y actualizar el estado de la UI         viewModelScope.launch {             mqttFlow.collect { tvMsg \-\>                 tvMsg ?: return@collect                 \_state.update { it.copy(                     fcActual \= tvMsg.bpm,                     fcEstado \= tvMsg.estado,                     ultimaHora \= tvMsg.hora,                     isLoading \= false                 )}             }         }     }       override fun onCleared() {         mqttSubscriber.disconnect()     } } *Commit: feat(tv/mqtt): add MqttTvSubscriber — subscribes to TV topic and updates TvUiState reactively*  |

# **7\. Verificación — Flujo completo de los 3 dispositivos**

|  EMULADOR WEAR OS Health Services slider → bpm cambia MqttWearPublisher.publishFC(bpm) ↓  topic: utng/smarthealthmonitor/fc ☁️  HiveMQ Cloud ↓ EMULADOR TELÉFONO MqttAppService.handleFcMessage() SmartHealthRepository.fcFlow.value \= bpm DashboardScreen se actualiza ← collectAsState() MqttAppService republishes → topic TV ↓  topic: utng/smarthealthmonitor/tv ☁️  HiveMQ Cloud ↓ EMULADOR ANDROID TV MqttTvSubscriber.messageArrived() TvViewModel.mqttFlow.emit(tvMsg) TvCatalogScreen se actualiza ← collectAsState()  |
| :---: |

| \# | Dispositivo | Verificación en Logcat / UI | ✓ |
| :---: | :---- | :---- | :---: |
| 1 | ⌚ Wear | Logcat wear: '✅ Conectado a HiveMQ Cloud' | ☐ |
| 2 | ⌚ Wear | Al mover el slider de Health Services → Logcat: '📤 Publicado: XX bpm' | ☐ |
| 3 | 📱 App | Logcat app: '✅ Conectado y suscrito a utng/smarthealthmonitor/fc' | ☐ |
| 4 | 📱 App | Logcat app: '🔁 Re-publicado al TV: XX bpm' cuando llega mensaje del reloj | ☐ |
| 5 | 📱 App | DashboardScreen muestra la FC actualizada sin reiniciar la app | ☐ |
| 6 | 📺 TV | Logcat tv: '✅ TV suscrita a utng/smarthealthmonitor/tv' | ☐ |
| 7 | 📺 TV | TvCatalogScreen muestra la FC actualizada en el título de la fila 1 | ☐ |
| 8 | 🔁 E2E | Mover el slider en Wear → en menos de 3 segundos la TV muestra el nuevo valor | ☐ |
| 9 | ☁️ MQTT | En MQTT Explorer: los 3 topics muestran mensajes en tiempo real | ☐ |

# **8\. Solución de Problemas Frecuentes**

| ❌ Error / Síntoma | ✅ Solución |
| :---- | :---- |
| ConnectionRefused / Network error | Verificar que la URL del broker empieza con ssl:// y el puerto es 8883, no 1883\. |
| Not authorized | Las credenciales están mal. Verificar en HiveMQ Cloud → Access Management que el usuario y contraseña son correctos. |
| SSL handshake failed | Agregar: socketFactory \= SSLSocketFactory.getDefault() en MqttConnectOptions. El emulador no tiene el certificado de HiveMQ por defecto. |
| La TV no recibe mensajes | Verificar en MQTT Explorer que el topic utng/smarthealthmonitor/tv recibe mensajes. Si sí llegan al broker pero no a la TV, revisar el CLIENT\_ID — dos dispositivos con el mismo clientId se expulsan mutuamente. |
| bpm siempre 0 en la TV | El reloj no está publicando. Verificar que MqttWearPublisher.connect() se llama en el init{} del ViewModel y que Logcat wear muestra '✅ Conectado'. |
| App crashea con NetworkOnMainThreadException | Paho MQTT debe conectarse en un hilo background. Usar Dispatchers.IO: viewModelScope.launch(Dispatchers.IO) { mqttPublisher.connect() } |

| Commit final: feat(mqtt): complete MQTT integration — Wear publishes, App bridges, TV subscribesgit tag \-a v2.3.0 \-m 'feat: MQTT HiveMQ Cloud integration across all 3 devices' |
| :---- |


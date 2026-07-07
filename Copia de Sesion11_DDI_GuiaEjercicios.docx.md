

| Sesión: | 11 · Android TV — Compose for TV: Catálogo \+ D-pad Navigation | SmartHealth TV |
| :---- | :---- | ----- |
| **Alumno(s):** |  | **Fecha:** |

| ⚠️  IMPORTANTE — CAMBIO DE STACK: Esta sesión usa Jetpack Compose for TV (androidx.tv:tv-material) en lugar de Leanback Library XML. El foco D-pad se maneja con Modifier.focusable() y Surface de androidx.tv, no con BrowseSupportFragment. |
| :---- |

# **Contexto y Objetivo de la Sesión**

En esta sesión integras el módulo tv al proyecto SmartHealth Monitor. La pantalla principal muestra una cuadrícula de cards con lecturas de FC provenientes del Room DAO, navegable completamente con el control remoto D-pad. El stack es 100% Compose for TV — sin XML, sin Fragments, sin Leanback Library.

| Elemento | Leanback (versión anterior) | Compose for TV ✅ (esta sesión) |
| :---- | :---- | :---- |
| Pantalla catálogo | BrowseSupportFragment \+ XML | **TvLazyColumn / TvLazyRow \+ Composables** |
| Tarjeta de contenido | CardPresenter \+ ImageCardView | **@Composable Card() con Surface TV** |
| Foco D-pad | isFocusable=true en onCreateViewHolder | **Modifier.focusable() en cualquier Composable** |
| Tema visual | leanback:browse\_row\_hdr | **MaterialTheme de TV automático** |
| Navegación | FragmentManager \+ addToBackStack | **NavHost \+ rememberNavController** |

# **Archivos de esta sesión**

| Archivo nuevo | Propósito |
| :---- | :---- |
| tv/presentation/TvCatalogScreen.kt | Pantalla principal: cuadrícula de cards con LazyRows |
| tv/presentation/FcCardItem.kt | Composable de tarjeta individual con Surface TV |
| tv/presentation/TvViewModel.kt | ViewModel TV — lee Flow\<List\<LecturaFC\>\> del Repository |
| tv/presentation/TvViewModelFactory.kt | Factory DI para el ViewModel |
| tv/domain/model/TvUiState.kt | Estado UI del módulo TV |
| tv/TVActivity.kt | Activity raíz con NavHost de TV |

# **Ejercicios**

| 1 | Agregar dependencias Compose for TV al módulo tv   *⭐ Obligatorio* |
| :---: | :---- |

Abre tv/build.gradle.kts y agrega las dependencias de Compose for TV:

| 📄  tv/build.gradle.kts |
| :---- |
| // Compose for TV — reemplaza Leanback Library implementation("androidx.tv:tv-foundation:1.0.0") implementation("androidx.tv:tv-material:1.0.0")   // Compose base (si no está ya) implementation(platform("androidx.compose:compose-bom:2024.12.01")) implementation("androidx.compose.ui:ui") implementation("androidx.compose.foundation:foundation") implementation("androidx.navigation:navigation-compose:2.8.5")   // ELIMINAR si existía: implementation("androidx.leanback:leanback:...") |
| *Commit: feat(tv): replace Leanback with Compose for TV dependencies* |

| 2 | TvUiState — Estado inmutable del módulo TV   *⭐ Obligatorio* |
| :---: | :---- |

| 📄  tv/domain/model/TvUiState.kt |
| :---- |
| package mx.utng.smarthealthmonitor.tv.domain.model import mx.utng.smarthealthmonitor.domain.model.LecturaFC   data class TvUiState(     val lecturas    : List\<LecturaFC\> \= emptyList(),     val fcActual    : Int             \= 0,     val isLoading   : Boolean         \= true,     val error       : String?         \= null, ) |
| *Commit: feat(tv): add TvUiState domain model* |

| 3 | TvViewModel — Reactivo con StateFlow   *⭐ Obligatorio* |
| :---: | :---- |

| 📄  tv/presentation/TvViewModel.kt |
| :---- |
| package mx.utng.smarthealthmonitor.tv.presentation import androidx.lifecycle.ViewModel; import androidx.lifecycle.viewModelScope import kotlinx.coroutines.flow.\*; import kotlinx.coroutines.launch import mx.utng.smarthealthmonitor.domain.repository.SmartHealthRepository import mx.utng.smarthealthmonitor.tv.domain.model.TvUiState   class TvViewModel(     private val repository: SmartHealthRepository ) : ViewModel() {       private val \_state \= MutableStateFlow(TvUiState())     val state: StateFlow\<TvUiState\> \= \_state.asStateFlow()       init {         // Observar historial reactivo del Room DAO         viewModelScope.launch {             repository.obtenerHistorial()                 .catch { e \-\> \_state.update{it.copy(error=e.message,isLoading=false)} }                 .collect { lecturas \-\>                     \_state.update { it.copy(lecturas=lecturas, isLoading=false) }                 }         }         // Observar FC actual (StateFlow del sensor)         viewModelScope.launch {             repository.fcActual.collect { bpm \-\>                 \_state.update { it.copy(fcActual \= bpm) }             }         }     } } |
| *Commit: feat(tv): add TvViewModel with reactive Flow from Room DAO and FC StateFlow* |

| 4 | FcCardItem — Tarjeta Compose for TV con foco D-pad   *⭐ Obligatorio* |
| :---: | :---- |

La tarjeta usa Surface de androidx.tv que gestiona el foco D-pad automáticamente con focusedContainerColor:

| 📄  tv/presentation/FcCardItem.kt |
| :---- |
| package mx.utng.smarthealthmonitor.tv.presentation import androidx.compose.foundation.layout.\* import androidx.compose.foundation.shape.RoundedCornerShape import androidx.tv.material3.\* import androidx.compose.ui.graphics.Color import mx.utng.smarthealthmonitor.domain.model.LecturaFC   @Composable fun FcCardItem(     lectura : LecturaFC,     onClick  : () \-\> Unit,     modifier : Modifier \= Modifier ) {     // Surface de androidx.tv maneja el foco D-pad automáticamente     Surface(         onClick \= onClick,         modifier \= modifier.width(200.dp).height(120.dp),         colors \= ClickableSurfaceDefaults.colors(             containerColor        \= Color(0xFF1565C0),   // azul sin foco             focusedContainerColor \= Color(0xFF42A5F5),   // azul claro con foco D-pad             pressedContainerColor \= Color(0xFF0D47A1),         ),         shape \= ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp))     ) {         Column(             modifier \= Modifier.fillMaxSize().padding(16.dp),             verticalArrangement \= Arrangement.SpaceBetween         ) {             Text("${lectura.bpm} bpm",                  style \= MaterialTheme.typography.headlineMedium,                  color \= Color.White,                  fontWeight \= FontWeight.Bold)             Column {                 Text(lectura.estado,                      style \= MaterialTheme.typography.bodyMedium,                      color \= Color.White.copy(alpha \= 0.8f))                 Text(lectura.hora,                      style \= MaterialTheme.typography.bodySmall,                      color \= Color.White.copy(alpha \= 0.6f))             }         }     } } |
| **La diferencia clave vs Leanback: NO necesitas isFocusable=true ni nextFocusDown en XML. Surface de androidx.tv gestiona el foco D-pad automáticamente con focusedContainerColor.** |
| *Commit: feat(tv): add FcCardItem composable with TV Surface D-pad focus* |

| 5 | TvCatalogScreen — Pantalla principal con TvLazyRow   *⭐ Obligatorio* |
| :---: | :---- |

| 📄  tv/presentation/TvCatalogScreen.kt |
| :---- |
| @Composable fun TvCatalogScreen(     viewModel: TvViewModel \= viewModel(factory \= TvViewModelFactory(LocalContext.current)) ) {     val state by viewModel.state.collectAsStateWithLifecycle()       Box(Modifier.fillMaxSize().background(Color(0xFF0D1B4A))) {           if (state.isLoading) {             CircularProgressIndicator(Modifier.align(Alignment.Center))             return@Box         }           TvLazyColumn(             modifier \= Modifier.fillMaxSize().padding(48.dp),             verticalArrangement \= Arrangement.spacedBy(32.dp)         ) {             // Fila 1: FC actual             item {                 RowSection(title \= "⚡ Estado Actual — ${state.fcActual} bpm") {                     TvLazyRow(horizontalArrangement=Arrangement.spacedBy(16.dp)) {                         items(state.lecturas.takeLast(3)) { lectura \-\>                             FcCardItem(lectura=lectura, onClick={})                         }                     }                 }             }               // Fila 2: Historial completo             item {                 RowSection(title \= "📋 Historial FC") {                     TvLazyRow(horizontalArrangement=Arrangement.spacedBy(16.dp)) {                         items(state.lecturas) { lectura \-\>                             FcCardItem(lectura=lectura, onClick={})                         }                     }                 }             }         }     } }   @Composable private fun RowSection(title: String, content: @Composable () \-\> Unit) {     Column(verticalArrangement \= Arrangement.spacedBy(12.dp)) {         Text(title, style=MaterialTheme.typography.headlineSmall,              color=Color.White, fontWeight=FontWeight.Bold)         content()     } } |
| *Commit: feat(tv): add TvCatalogScreen with TvLazyColumn/TvLazyRow and two data rowsgit tag \-a v2.0.0 \-m 'feat: Android TV module — Compose for TV catalog screen'* |

| 6 | Verificación — Navegar con D-pad en el emulador Android TV   *⭐ Entrega* |
| :---: | :---- |

| ✅ Verifica que funciona | ❌ Error frecuente |
| :---- | :---- |
| Las dos filas de cards son visibles en el AVD Android TV. | El AVD lanza pero no muestra datos → verificar que Room tiene datos del proyecto app. |
| Presionar ←→ navega entre cards en una fila (foco azul claro). | Cards sin foco visible → verificar que usas Surface de androidx.tv, no Button de Material3. |
| Presionar ↑↓ cambia entre las dos filas. | NullPointerException en TvViewModel → verificar que el Repository está inyectado correctamente. |
| La FC actual (state.fcActual) se actualiza en el título de la Fila 1\. | FC siempre 0 → verificar que repository.fcActual es el mismo StateFlow del módulo app. |


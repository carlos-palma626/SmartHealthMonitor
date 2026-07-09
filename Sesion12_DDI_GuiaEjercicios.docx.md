

| Sesión: | 12 · Android TV — DetailScreen \+ Media3/ExoPlayer con Compose | SmartHealth TV |
| :---- | :---- | ----- |
| **Alumno(s):** |  | **Fecha:** |

| Esta sesión usa Compose for TV y Navigation Compose. La pantalla de detalle (DetailScreen) se navega al presionar OK sobre una card. El reproductor usa AndroidView \+ ExoPlayer dentro de un Composable. |
| :---- |

# **Contexto de la Sesión**

Al presionar OK sobre una FcCardItem en TvCatalogScreen, el NavController navega a TvDetailScreen. Esta pantalla muestra el detalle de una lectura de FC con imagen, datos y botones de acción navegables con D-pad. El botón 'Reproducir' abre TvPlaybackScreen con ExoPlayer.

| Pantalla | Leanback (anterior) | Compose for TV ✅ |
| :---- | :---- | :---- |
| Detalle | DetailsSupportFragment \+ DetailsOverviewRow | **@Composable DetailScreen con Column** |
| Botones de acción | Action(id, label) \+ onActionClicked() | **Button/Surface TV con onClick lambda** |
| Reproductor | PlaybackSupportFragment \+ LeanbackPlayerAdapter | **AndroidView { PlayerView(it) } \+ ExoPlayer** |
| Navegación al detalle | addToBackStack \+ replace() | **navController.navigate("detail/{id}")** |

# **Ejercicios**

| 1 | Configurar Navigation Compose para TV   *⭐ Obligatorio* |
| :---: | :---- |

Actualiza TVActivity.kt para incluir las rutas al detalle y al reproductor:

| 📄  tv/TVActivity.kt |
| :---- |
| class TVActivity : ComponentActivity() {     override fun onCreate(savedInstanceState: Bundle?) {         super.onCreate(savedInstanceState)         setContent {             SmartHealthTvTheme {                 val navController \= rememberNavController()                 NavHost(navController, startDestination \= "catalog") {                     composable("catalog") {                         TvCatalogScreen(onCardClick \= { lecturaId \-\>                             navController.navigate("detail/$lecturaId")                         })                     }                     composable(                         route \= "detail/{lecturaId}",                         arguments \= listOf(navArgument("lecturaId") { type=NavType.IntType })                     ) { backStack \-\>                         val id \= backStack.arguments?.getInt("lecturaId") ?: return@composable                         TvDetailScreen(lecturaId=id, navController=navController)                     }                     composable("playback") {                         TvPlaybackScreen(navController=navController)                     }                 }             }         }     } } |
| *Commit: feat(tv): add Navigation Compose with catalog/detail/playback routes* |

| 2 | TvDetailScreen — Pantalla de detalle con Compose for TV   *⭐ Obligatorio* |
| :---: | :---- |

| 📄  tv/presentation/TvDetailScreen.kt |
| :---- |
| @Composable fun TvDetailScreen(     lecturaId   : Int,     navController: NavController,     viewModel   : TvViewModel \= viewModel(factory=TvViewModelFactory(LocalContext.current)) ) {     val state by viewModel.state.collectAsStateWithLifecycle()     val lectura \= state.lecturas.find { it.id \== lecturaId } ?: return       // FocusRequester para mover el foco al primer botón al entrar     val firstBtnFocus \= remember { FocusRequester() }     LaunchedEffect(Unit) { firstBtnFocus.requestFocus() }       Row(Modifier.fillMaxSize().background(Color(0xFF0D1B4A)).padding(64.dp),         horizontalArrangement \= Arrangement.spacedBy(48.dp)) {           // Panel izquierdo — ícono \+ datos         Column(Modifier.weight(0.4f), verticalArrangement=Arrangement.spacedBy(16.dp)) {             Box(Modifier.size(200.dp).background(Color(0xFF1565C0),CircleShape),                 contentAlignment=Alignment.Center) {                 Text("❤", fontSize=80.sp)             }             Text("${lectura.bpm} bpm",                  style=MaterialTheme.typography.displayMedium,                  color=Color.White, fontWeight=FontWeight.ExtraBold)             Text("Estado: ${lectura.estado}",                  style=MaterialTheme.typography.bodyLarge, color=Color.White.copy(0.8f))             Text("Hora: ${lectura.hora}",                  style=MaterialTheme.typography.bodyMedium, color=Color.White.copy(0.6f))         }           // Panel derecho — botones de acción         Column(Modifier.weight(0.6f), verticalArrangement=Arrangement.spacedBy(20.dp),                horizontalAlignment=Alignment.CenterHorizontally) {               Spacer(Modifier.weight(1f))               // Botón Reproducir             Surface(onClick \= { navController.navigate("playback") },                     modifier=Modifier.focusRequester(firstBtnFocus)                               .fillMaxWidth(0.7f).height(60.dp),                     colors=ClickableSurfaceDefaults.colors(                         containerColor=Color(0xFF1B5E20),                         focusedContainerColor=Color(0xFF76FF03)),                     shape=ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))) {                 Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {                     Text("▶  Reproducir",color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Bold)                 }             }               // Botón Volver             Surface(onClick \= { navController.popBackStack() },                     modifier=Modifier.fillMaxWidth(0.7f).height(60.dp),                     colors=ClickableSurfaceDefaults.colors(                         containerColor=Color(0xFF37474F),                         focusedContainerColor=Color(0xFF90A4AE)),                     shape=ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))) {                 Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {                     Text("← Volver",color=Color.White,fontSize=18.sp)                 }             }               Spacer(Modifier.weight(1f))         }     } } |
| *Commit: feat(tv): add TvDetailScreen with two focusable action buttons* |

| 3 | TvPlaybackScreen — ExoPlayer con AndroidView dentro de Compose   *⭐ Obligatorio* |
| :---: | :---- |

ExoPlayer no tiene Composable nativo. Se integra con AndroidView que envuelve un PlayerView del View system:

| 📄  tv/presentation/TvPlaybackScreen.kt |
| :---- |
| @Composable fun TvPlaybackScreen(navController: NavController) {     val ctx \= LocalContext.current       // Crear ExoPlayer ligado al ciclo de vida del Composable     val exoPlayer \= remember {         ExoPlayer.Builder(ctx).build().apply {             val mediaItem \= MediaItem.fromUri(                 "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"             )             setMediaItem(mediaItem)             prepare()             playWhenReady \= true         }     }       // CRÍTICO: liberar ExoPlayer al salir del Composable     DisposableEffect(Unit) {         onDispose {             exoPlayer.release()  // equivalente a onDestroyView en Fragment         }     }       Box(Modifier.fillMaxSize().background(Color.Black)) {           // AndroidView envuelve el PlayerView del View system         AndroidView(             factory \= { context \-\>                 PlayerView(context).apply {                     player \= exoPlayer                     useController \= true                 }             },             modifier \= Modifier.fillMaxSize()         )           // Botón Back en esquina superior izquierda         Surface(onClick \= { exoPlayer.stop(); navController.popBackStack() },                 modifier \= Modifier.align(Alignment.TopStart).padding(24.dp),                 colors \= ClickableSurfaceDefaults.colors(                     containerColor=Color(0x88000000),                     focusedContainerColor=Color(0xCCFFFFFF))) {             Text("← Volver", color=Color.White, modifier=Modifier.padding(12.dp))         }     } } |
| **DisposableEffect es el equivalente de onDestroyView en Compose. El bloque onDispose() se ejecuta cuando el Composable sale de la composición — aquí liberamos ExoPlayer para evitar que el audio continúe.** |
| *Commit: feat(tv): add TvPlaybackScreen with ExoPlayer via AndroidView and DisposableEffectgit tag \-a v2.1.0 \-m 'feat: TV detail and playback screens with ExoPlayer'* |

| 4 | Verificación — Flujo completo catálogo → detalle → reproductor   *⭐ Entrega* |
| :---: | :---- |

| Paso | Acción / Verificación | ✓ |
| :---: | :---- | :---: |
| 1 | TvCatalogScreen muestra las dos filas de cards con datos de Room. | ☐ |
| 2 | Presionar OK sobre una card navega a TvDetailScreen (no cierra la app). | ☐ |
| 3 | TvDetailScreen muestra FC, estado y hora correctos de la lectura seleccionada. | ☐ |
| 4 | Presionar OK sobre '▶ Reproducir' navega a TvPlaybackScreen y el video inicia. | ☐ |
| 5 | Presionar Back desde TvPlaybackScreen vuelve a TvDetailScreen (ExoPlayer liberado). | ☐ |
| 6 | Presionar Back desde TvDetailScreen vuelve a TvCatalogScreen con el foco en la card. | ☐ |


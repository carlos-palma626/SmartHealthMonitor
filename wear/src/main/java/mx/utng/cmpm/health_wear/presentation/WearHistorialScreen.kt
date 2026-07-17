package mx.utng.cmpm.health_wear.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import mx.utng.cmpm.smarthealthmonitor.data.db.LecturaFC

@Composable  
fun WearHistorialScreen(  
    onBack: () -> Unit,  
    viewModel: WearDashboardViewModel = viewModel()  
) {  
    val historial by viewModel.historial.collectAsState()  
    val listState = rememberScalingLazyListState()  
    val focusRequester = remember { FocusRequester() }  
   
    // Pedir foco para recibir eventos de la corona y actualizar datos
    LaunchedEffect(Unit) {  
        focusRequester.requestFocus()  
        viewModel.refreshHistorial()
    }  
   
    Scaffold(  
        timeText = {  
            TimeText()  
        },  
        positionIndicator = {  
            PositionIndicator(scalingLazyListState = listState)  
        }  
    ) {  
        ScalingLazyColumn(  
            state    = listState,  
            modifier = Modifier  
                .fillMaxSize()  
                .rotaryScrollable(  // ← conecta la corona  
                    behavior = RotaryScrollableDefaults.behavior(  
                        scrollableState = listState  
                    ),  
                    focusRequester = focusRequester  
                ),
            flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = listState),
            contentPadding = PaddingValues(
                top = 24.dp,
                start = 8.dp,
                end = 8.dp,
                bottom = 24.dp
            )
        ) {  
            item {  
                Text("Historial (${historial.size})",  
                     style = MaterialTheme.typography.title3,  
                     modifier = Modifier.padding(8.dp))  
            }  
            if (historial.isEmpty()) {  
                item {  
                    Text("Sin lecturas aún",  
                         style = MaterialTheme.typography.body2,  
                         modifier = Modifier.padding(8.dp))  
                }  
            } else {  
                items(historial, key = { it.id }) { lectura ->  
                    WearFilaHistorial(lectura = lectura)  
                }  
            }  
        }  
    }  
}  

@Composable  
fun WearFilaHistorial(lectura: LecturaFC) {  
    val color = if (lectura.estado == "Normal")  
        MaterialTheme.colors.primary  
    else  
        MaterialTheme.colors.error  
   
    Chip(  
        label = { Text("${lectura.bpm} bpm",  
                       color = color) },  
        secondaryLabel = { Text(lectura.hora) },  
        onClick = { },  
        colors = ChipDefaults.secondaryChipColors(),  
        modifier = Modifier.fillMaxWidth()  
    )  
}  

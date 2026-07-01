package mx.utng.cmpm.health_wear.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import mx.utng.cmpm.health_wear.presentation.components.WearFCCard

@Composable  
fun WearDashboardScreen(  
    onAlertClick: () -> Unit = {},  
    onHistorialClick: () -> Unit = {},  
    viewModel: WearDashboardViewModel = viewModel()  
) {  
    val fc by viewModel.fc.collectAsState()  
    val pasos by viewModel.pasos.collectAsState()
    val listState = rememberScalingLazyListState()  
   
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 24.dp,
                start = 8.dp,
                end = 8.dp,
                bottom = 24.dp
            )
        ) {  
            // Item 1: Card de FC  
            item {  
                WearFCCard(  
                    fc = fc,  
                    modifier = Modifier.fillMaxWidth()  
                )  
            }
            
            // Reto Adicional: Conteo de pasos del día (CompactChip)
            item {
                val pasosTexto = if (pasos == 0) "-- pasos" else "$pasos pasos"
                CompactChip(
                    label = { Text(pasosTexto) },
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
            
            // Item 3: Chip de Historial
            item {
                Chip(
                    label = { Text("📋 Historial") },
                    onClick = onHistorialClick,
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Item 4: Chip de Alerta  
            item {  
                Chip(  
                    label  = { Text("⚠ Alerta") },  
                    onClick = onAlertClick,  
                    colors = ChipDefaults.primaryChipColors(  
                        backgroundColor = MaterialTheme.colors.error  
                    ),  
                    modifier = Modifier.fillMaxWidth()  
                )  
            }  
        }  
    }  
}  

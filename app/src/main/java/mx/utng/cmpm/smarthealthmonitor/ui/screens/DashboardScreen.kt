package mx.utng.cmpm.smarthealthmonitor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.utng.cmpm.smarthealthmonitor.data.db.LecturaFC
import mx.utng.cmpm.smarthealthmonitor.data.models.MockData
import mx.utng.cmpm.smarthealthmonitor.ui.components.FilaHistorial
import mx.utng.cmpm.smarthealthmonitor.ui.components.TarjetaDato
import mx.utng.cmpm.smarthealthmonitor.ui.theme.SmartHealthMonitorTheme
import androidx.compose.ui.text.font.FontWeight

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import mx.utng.cmpm.smarthealthmonitor.ui.viewmodel.DashboardViewModel

import mx.utng.cmpm.smarthealthmonitor.data.SmartHealthRepository

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onHistorialClick: () -> Unit = {},
    onAlertClick: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val fc by viewModel.fc.collectAsState()
    val pasos by viewModel.pasos.collectAsState()
    val historial by viewModel.historial.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SmartHealth",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary // CORREGIDO: Se añadió MaterialTheme.colorScheme
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAlertClick,
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Enviar alerta de emergencia",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

// Tarjeta FC
            item {
                val isNormal = fc in 60..100 || fc == 0
                TarjetaDato(
                    valor = "$fc",
                    unidad = "bpm",
                    label = "Frecuencia cardíaca",
                    colorValor = if (isNormal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
// Tarjeta Pasos
            item {
                TarjetaDato(
                    valor = "%,d".format(pasos),
                    unidad = "pasos",
                    label = "Pasos del día",
                    colorValor = MaterialTheme.colorScheme.primary
                )
            }
// Encabezado historial
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historial reciente",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onHistorialClick) {
                        Text("Ver todo")
                    }
                }
            }
// Lista del historial
            items(historial, key = { it.id }) { lectura ->
                FilaHistorial(lectura = lectura)
            }

        }
    }
}

@Preview(showBackground = true, name = "Dashboard - Light", showSystemUi = true, device = "id:pixel_6")
@Preview(showBackground = true, name = "Dashboard - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showSystemUi = true, device = "id:pixel_6")
@Composable
private fun DashboardScreenPreview() {
    SmartHealthMonitorTheme {
        DashboardScreen()
    }
}
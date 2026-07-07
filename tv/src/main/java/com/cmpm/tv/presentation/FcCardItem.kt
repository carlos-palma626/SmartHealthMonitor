package com.cmpm.tv.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.tv.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import com.cmpm.tv.domain.model.LecturaFC

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FcCardItem(
    lectura : LecturaFC,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier
) {
    // Surface de androidx.tv maneja el foco D-pad automáticamente
    Surface(
        onClick = onClick,
        modifier = modifier.width(200.dp).height(120.dp),
        colors = ClickableSurfaceDefaults.colors(
            containerColor        = Color(0xFF1565C0),   // azul sin foco
            focusedContainerColor = Color(0xFF42A5F5),   // azul claro con foco D-pad
            pressedContainerColor = Color(0xFF0D47A1),
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${lectura.bpm} bpm",
                 style = MaterialTheme.typography.headlineMedium,
                 color = Color.White,
                 fontWeight = FontWeight.Bold)
            Column {
                Text(lectura.estado,
                     style = MaterialTheme.typography.bodyMedium,
                     color = Color.White.copy(alpha = 0.8f))
                Text(lectura.hora,
                     style = MaterialTheme.typography.bodySmall,
                     color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

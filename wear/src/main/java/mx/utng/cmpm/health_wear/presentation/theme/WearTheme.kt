package mx.utng.cmpm.health_wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun SmartHealthWearTheme(
    content: @Composable () -> Unit
) {
    // Wear Material Theme — versión circular de MD3
    MaterialTheme(
        content = content
    )
}

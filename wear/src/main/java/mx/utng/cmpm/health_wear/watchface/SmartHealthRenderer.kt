package mx.utng.cmpm.health_wear.watchface

import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import androidx.wear.watchface.*
import androidx.wear.watchface.style.*
import java.time.ZonedDateTime
import mx.utng.cmpm.smarthealthmonitor.data.SmartHealthRepository

class SmartHealthRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    interactiveDrawModeUpdateDelayMillis: Long
) : Renderer.CanvasRenderer2<Renderer.SharedAssets>(
    surfaceHolder, currentUserStyleRepository, watchState,
    CanvasType.HARDWARE, interactiveDrawModeUpdateDelayMillis,
    true // clearWithBackgroundTintBeforeRenderingHighlightLayer
) {

    private val paintHora = Paint().apply {
        color     = Color.WHITE
        textSize  = 72f
        isAntiAlias = true
        typeface  = Typeface.DEFAULT_BOLD
    }
    
    private val paintHoraAmbient = Paint().apply {
        color     = Color.WHITE
        textSize  = 72f
        isAntiAlias = false
        typeface  = Typeface.DEFAULT_BOLD
    }

    private val paintFC = Paint().apply {
        color     = Color.RED
        textSize  = 30f
        isAntiAlias = true
    }

    private val paintSub = Paint().apply {
        color     = Color.GRAY
        textSize = 22f
        isAntiAlias = true
    }

    override suspend fun createSharedAssets(): SharedAssets =
        object : SharedAssets { override fun onDestroy() {} }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        // Fondo negro — ahorra batería en modo AOD
        canvas.drawColor(Color.BLACK)

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()

        // Modo AOD / AMBIENT (Reto Adicional)
        val isAmbient = renderParameters.drawMode == DrawMode.AMBIENT

        // Hora digital centrada
        val hora = String.format("%02d:%02d", zonedDateTime.hour, zonedDateTime.minute)
        
        if (isAmbient) {
            val tw = paintHoraAmbient.measureText(hora)
            canvas.drawText(hora, cx - tw / 2, cy + 24f, paintHoraAmbient)
            // En modo ambiente no dibujamos segundos ni FC para ahorrar batería
        } else {
            val tw = paintHora.measureText(hora)
            canvas.drawText(hora, cx - tw / 2, cy - 10f, paintHora)

            // Segundos (pequeño debajo)
            val seg = String.format("%02d", zonedDateTime.second)
            canvas.drawText(seg, cx - 18f, cy + 30f, paintSub)

            // FC desde SmartHealthRepository
            val fc = SmartHealthRepository.fcFlow.value
            if (fc > 0) {
                val fcStr = "❤ $fc bpm"
                val fcW = paintFC.measureText(fcStr)
                canvas.drawText(fcStr, cx - fcW / 2, cy + 70f, paintFC)
            }
        }
    }

    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        canvas.drawColor(renderParameters.highlightLayer!!.backgroundTint)
    }
}

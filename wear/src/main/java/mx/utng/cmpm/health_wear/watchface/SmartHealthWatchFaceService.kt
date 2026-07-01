package mx.utng.cmpm.health_wear.watchface

import android.view.SurfaceHolder
import androidx.wear.watchface.*
import androidx.wear.watchface.style.*

class SmartHealthWatchFaceService : WatchFaceService() {

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = SmartHealthRenderer(
            context                    = applicationContext,
            surfaceHolder              = surfaceHolder,
            watchState                 = watchState,
            complicationSlotsManager   = complicationSlotsManager,
            currentUserStyleRepository = currentUserStyleRepository,
            interactiveDrawModeUpdateDelayMillis = 1000L
        )
        return WatchFace(WatchFaceType.DIGITAL, renderer)
    }
}

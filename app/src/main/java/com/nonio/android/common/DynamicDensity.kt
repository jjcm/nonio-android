package com.nonio.android.common

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Calculate dynamic density to adapt to different screens based on UI design
 * @param designWidth Enter the short edge dp value (absolute width) of the UI design screen
 * @param designHeight Enter the long edge dp value (absolute height) of the UI design screen
 */
@Composable
fun dynamicDensity(
    designWidth: Float,
    designHeight: Float,
): Float {
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val widthPixels = displayMetrics.widthPixels // Screen short edge pixels (absolute width)
    val heightPixels = displayMetrics.heightPixels // Screen long edge pixels (absolute height)
    val isPortrait =
        LocalContext.current.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT // Determine portrait or landscape orientation
    return if (isPortrait) widthPixels / designWidth else heightPixels / designHeight // Calculate density
}

package com.nonio.android.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun getScreenWidth(): Float {
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val widthPixels = displayMetrics.widthPixels
    val density = LocalContext.current.resources.displayMetrics.density
    return widthPixels / density
}

@Composable
fun getScreenHeight(): Float {
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val heightPixels = displayMetrics.heightPixels
    val density = LocalContext.current.resources.displayMetrics.density
    return heightPixels / density
}

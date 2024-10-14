package com.nonio.android.common

import android.content.Context
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import java.text.DecimalFormat

@Composable
fun px2dp(px: Float): Dp {
    with(LocalDensity.current) { return px.toDp() }
}

fun Int.dp2px(context: Context): Int =
    (
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics,
        ) + 0.5f
    ).toInt()

fun Int.sp2px(context: Context): Int =
    (
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this.toFloat(),
            context.resources.displayMetrics,
        ) + 0.5f
    ).toInt()

fun Long.fileSize(): String {
    val df = DecimalFormat("#.00")
    if (this <= 0) {
        return "0B"
    }
    return if (this < 1024) {
        df.format(this) + "B"
    } else if (this < 1048576) {
        df.format(this.toDouble() / 1024) + "KB"
    } else if (this < 1073741824) {
        df.format(this.toDouble() / 1048576) + "MB"
    } else {
        df.format(this.toDouble() / 1073741824) + "GB"
    }
}

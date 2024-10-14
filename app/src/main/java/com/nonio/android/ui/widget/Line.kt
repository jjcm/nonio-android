package com.nonio.android.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Line(
    modifier: Modifier = Modifier,
    color: Color = colorScheme.secondary,
    width: Dp = 10000.dp,
    height: Dp = 0.5.dp,
) {
    Box(
        modifier =
            modifier
                .background(color)
                .height(height)
                .width(width),
    )
}

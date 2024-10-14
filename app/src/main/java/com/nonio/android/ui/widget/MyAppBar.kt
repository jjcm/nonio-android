package com.nonio.android.ui.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MyAppBar(
    leading: @Composable RowScope.() -> Unit = {},
    title: @Composable () -> Unit = {},
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(44.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            leading()
            trailing()
        }
        title()
    }
}

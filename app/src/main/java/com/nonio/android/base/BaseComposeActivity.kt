package com.nonio.android.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.nonio.android.common.dynamicDensity
import com.nonio.android.common.transparentStatusAndNavigation
import com.nonio.android.ui.theme.AppTheme

open class BaseComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparentStatusAndNavigation()
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                val fontScale = LocalDensity.current.fontScale
                val appDensity =
                    Density(density = dynamicDensity(393F, 821F), fontScale = fontScale)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CompositionLocalProvider(
                        LocalDensity provides appDensity,
                        content = initComposeView(),
                    )
                }
            }
        }
    }

    open fun initComposeView(): @Composable () -> Unit = {}
}

/*
 * Copyright 2023 Dora Lee
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nonio.android.video

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.nonio.android.video.controller.VideoPlayerControllerConfig
import com.nonio.android.video.controller.applyToExoPlayerView
import com.nonio.android.video.util.findActivity
import com.nonio.android.video.util.setFullScreen

/**
 * ExoPlayer does not support full screen views by default.
 * So create a full screen modal that wraps the Compose Dialog.
 *
 * Delegate all functions of the video controller that were used just before
 * the full screen to the video controller managed by that component.
 * Conversely, if the full screen dismissed, it will restore all the functions it delegated
 * for synchronization with the video controller on the full screen and the video controller on the previous screen.
 *
 * @param player Exoplayer instance.
 * @param currentPlayerView [androidx.media3.ui.PlayerView] instance currently in use for playback.
 * @param fullScreenPlayerView Callback to return all features to existing video player controller.
 * @param controllerConfig Player controller config. You can customize the Video Player Controller UI.
 * @param repeatMode Sets the content repeat mode.
 * @param enablePip Enable PIP.
 * @param onDismissRequest Callback that occurs when modals are closed.
 * @param securePolicy Policy on setting [android.view.WindowManager.LayoutParams.FLAG_SECURE] on a full screen dialog window.
 */
@SuppressLint("UnsafeOptInUsageError")
@Composable
internal fun VideoPlayerFullScreenDialog(
    player: ExoPlayer,
    currentPlayerView: PlayerView,
    fullScreenPlayerView: PlayerView.() -> Unit,
    controllerConfig: VideoPlayerControllerConfig,
    repeatMode: RepeatMode,
    resizeMode: ResizeMode,
    enablePip: Boolean,
    onDismissRequest: () -> Unit,
    securePolicy: SecureFlagPolicy,
) {
    val context = LocalContext.current
    val internalFullScreenPlayerView =
        remember {
            PlayerView(context)
                .also(fullScreenPlayerView)
        }
    var isFullScreenModeEntered by remember {
        mutableStateOf(false)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties =
            DialogProperties(
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = true,
                securePolicy = securePolicy,
                decorFitsSystemWindows = false,
            ),
    ) {
        LaunchedEffect(Unit) {
            PlayerView.switchTargetView(player, currentPlayerView, internalFullScreenPlayerView)

            val currentActivity = context.findActivity()
            currentActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        val activityWindow = getActivityWindow()
        val dialogWindow = getDialogWindow()

        SideEffect {
            if (activityWindow != null && dialogWindow != null && !isFullScreenModeEntered) {
                activityWindow.setFullScreen(true)
                dialogWindow.setFullScreen(true)
                // dialogWindow has extra padding but activityWindow doesn't;
                // copy the attributes from activity to dialog
                WindowManager.LayoutParams().apply {
                    copyFrom(activityWindow.attributes)
                    type = dialogWindow.attributes.type
                    dialogWindow.attributes = this
                }
                isFullScreenModeEntered = true
            }
        }

        LaunchedEffect(controllerConfig) {
            controllerConfig.applyToExoPlayerView(internalFullScreenPlayerView) {
                if (!it) {
                    onDismissRequest()
                }
            }
            internalFullScreenPlayerView
                .findViewById<ImageButton>(androidx.media3.ui.R.id.exo_fullscreen)
                .performClick()
        }

        LaunchedEffect(controllerConfig, repeatMode) {
            internalFullScreenPlayerView.setRepeatToggleModes(
                if (controllerConfig.showRepeatModeButton) {
                    RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL or RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE
                } else {
                    RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE
                },
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black),
        ) {
            VideoPlayerSurface(
                defaultPlayerView = internalFullScreenPlayerView,
                player = player,
                usePlayerController = true,
                handleLifecycle = !enablePip,
                autoDispose = false,
                enablePip = enablePip,
                surfaceResizeMode = resizeMode,
                onPipEntered = { onDismissRequest() },
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(),
            )
        }
    }
}

@Composable
internal fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window

@Composable
internal fun getActivityWindow(): Window? =
    LocalView.current.context
        .findActivity()
        .window

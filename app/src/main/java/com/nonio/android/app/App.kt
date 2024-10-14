package com.nonio.android.app

import android.app.Application
import androidx.media3.exoplayer.ExoPlayer
import com.nonio.android.BuildConfig
import com.nonio.android.common.ApplicationScopeViewModelProvider
import com.nonio.android.common.UserHelper
import timber.log.Timber

class App : Application() {
    companion object {
        lateinit var app: App
        lateinit var player: ExoPlayer
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        initTimber()
        ApplicationScopeViewModelProvider.init(this)
        UserHelper.init(this.applicationContext)

        player = ExoPlayer.Builder(this).build()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    fun getPlayer(): ExoPlayer = player

//    private fun initCoil() {
//        imageLoader = ImageLoader.Builder(this)
//            .components {
//                if (SDK_INT >= 28) {
//                    add(ImageDecoderDecoder.Factory())
//                } else {
//                    add(GifDecoder.Factory())
//                }
//            }
//            .build()
//
//    }
}

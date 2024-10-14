package com.nonio.android.common

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.media3.exoplayer.ExoPlayer
import com.nonio.android.app.App
import com.nonio.android.model.LikeEvent
import com.nonio.android.model.TagEvent
import com.nonio.android.model.VoteModel
import com.nonio.android.model.VotesModel
import com.nonio.android.network.NetworkHelper.apiService
import kotlinx.coroutines.flow.MutableSharedFlow

object ApplicationScopeViewModelProvider : ViewModelStoreOwner {
    private val eventViewModelStore: ViewModelStore = ViewModelStore()

    private lateinit var mApplication: Application

    private val mApplicationProvider: ViewModelProvider by lazy {
        ViewModelProvider(
            ApplicationScopeViewModelProvider,
            ViewModelProvider.AndroidViewModelFactory.getInstance(mApplication),
        )
    }

    fun <T : ViewModel> getApplicationScopeViewModel(modelClass: Class<T>): T {
        ViewModelProvider.AndroidViewModelFactory
        return mApplicationProvider[modelClass]
    }

    fun init(application: Application) {
        mApplication = application
    }

    override val viewModelStore: ViewModelStore
        get() = eventViewModelStore
}

fun appViewModel(): AppViewModel = ApplicationScopeViewModelProvider.getApplicationScopeViewModel(AppViewModel::class.java)

class AppViewModel(
    application: Application,
) : AndroidViewModel(application = application) {
    var isLogin by mutableStateOf(UserHelper.isLogin())

    private var votes by mutableStateOf<VotesModel?>(null)

    val likeEventFlow = MutableSharedFlow<LikeEvent>()

    val tagEventFlow = MutableSharedFlow<TagEvent>()

    var notificationCount by mutableIntStateOf(0)

    var updateNotification by mutableStateOf(false)

    fun updateNotification() {
        updateNotification = !updateNotification
    }

    fun isLiked(
        postId: String?,
        tagId: String?,
    ): Boolean {
        votes?.let { votes ->
            return votes.votes.any { it.postID == postId && it.tagID == tagId }
        }
        return false
    }

    suspend fun refreshVote() {
        if (!isLogin) {
            votes = null
        } else {
            runCatching {
                votes = apiService.getVotes()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun updateVote(
        vote: VoteModel,
        isAdd: Boolean,
    ) {
        votes?.let { votes ->
            if (isAdd) {
                this.votes =
                    votes.copy(
                        votes = votes.votes.plus(vote),
                    )
            } else {
                this.votes =
                    votes.copy(
                        votes = votes.votes.minus(vote),
                    )
            }
        }
    }

    fun getPlayer(): ExoPlayer = App.app.getPlayer()
}

package com.nonio.android.ui.posts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.nonio.android.common.appViewModel
import com.nonio.android.model.PostModel
import com.nonio.android.model.PostSortModel
import com.nonio.android.model.PostSortType
import com.nonio.android.model.PostTimeType
import com.nonio.android.model.PostType
import com.nonio.android.model.TagModel
import com.nonio.android.model.VoteModel
import com.nonio.android.network.NetworkHelper.apiService
import com.nonio.android.network.toRequestBody
import com.nonio.android.ui.posts.tag.TagViewModel.Companion.currentTag
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

private class PostSource(
    val tagProvider: () -> String?,
    val sortProvider: () -> String?,
    val timeProvider: () -> String?,
    val userProvider: () -> String?,
) : PagingSource<Int, PostModel>() {
    override fun getRefreshKey(state: PagingState<Int, PostModel>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PostModel> { //

        val nextPage = params.key ?: 0
        runCatching {
            val postsModel =
                apiService.posts(
                    offset = nextPage,
                    tag = tagProvider(),
                    sort = sortProvider(),
                    time = timeProvider(),
                    user = userProvider(),
                )
            val list = postsModel?.postModels ?: emptyList()
            return LoadResult.Page(
                data = list,
                prevKey = if (nextPage >= 100) nextPage - 100 else null,
                nextKey = if (list.size >= 100) nextPage + 100 else null,
            )
        }.onFailure {
            return LoadResult.Error(it)
        }
        return LoadResult.Error(Exception("load error"))
    }
}

class PostsViewModelFactory(
    private val isUser: Boolean,
    private val userName: String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostsViewModel::class.java)) {
            return PostsViewModel(isUser, userName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PostsViewModel(
    val isUser: Boolean,
    private val userName: String,
) : ViewModel() {
    private val _currentSort = MutableStateFlow(PostSortModel(sort = PostSortType.POPULAR))
    val currentSort = _currentSort.asStateFlow()

    val posts =
        Pager(config = PagingConfig(10)) {
            PostSource(
                tagProvider = { currentTag.value?.tag },
                sortProvider = {
                    currentSort.value.sort.type
                        .lowercase()
                },
                timeProvider = {
                    currentSort.value.time
                        ?.type
                        ?.lowercase()
                },
                userProvider = {
                    if (isUser) {
                        userName
                    } else {
                        null
                    }
                },
            )
        }.flow.cachedIn(viewModelScope)

    private val _uiActions = MutableSharedFlow<UiAction>()

    val uiActions =
        _uiActions
            .asSharedFlow()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
            )

    init {
        viewModelScope.launch {
            appViewModel().refreshVote()
            currentTag
                .combine(currentSort) { _, _ ->
                    UiAction.Refresh
                }.collect {
                    _uiActions.emit(UiAction.Refresh)
                }
        }
    }

    fun updateSort(
        sortType: PostSortType,
        timeType: PostTimeType? = null,
    ) {
        _currentSort.value = PostSortModel(sort = sortType, time = timeType)
    }

    override fun onCleared() {
        playerCache.values.forEach { it.release() }
        playerCache.clear()
        super.onCleared()
    }

    fun like(
        post: PostModel,
        tagModel: TagModel,
    ) {
        viewModelScope.launch {
            runCatching {
                val isAddVote = !appViewModel().isLiked(tagModel.postID, tagModel.tagID)
                val requestBody = mapOf("post" to post.url, "tag" to tagModel.tag).toRequestBody()
                if (isAddVote) {
                    apiService.addTagVote(requestBody)?.let {
                        appViewModel().updateVote(it, true)
                    }
                } else {
                    apiService.removeTagVote(requestBody)?.let {
                        appViewModel().updateVote(
                            VoteModel(
                                postID = tagModel.postID,
                                tagID = tagModel.tagID,
                            ),
                            false,
                        )
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    private val playerCache = mutableMapOf<Int, ExoPlayer>()
    private val maxCacheSize = 3

    private val _currentPlayingIndex = MutableStateFlow<Int?>(null)
    val currentPlayingIndex: StateFlow<Int?> = _currentPlayingIndex

    fun updatePlayingIndex(
        firstVisibleIndex: Int,
        lastVisibleIndex: Int,
        videoItems: List<PostModel>,
    ) {
        val videoIndex =
            (firstVisibleIndex..lastVisibleIndex)
                .firstOrNull { videoItems[it].getPostType() == PostType.VIDEO }

        if (videoIndex != _currentPlayingIndex.value) {
            _currentPlayingIndex.value = videoIndex
        }
    }

    fun getOrCreatePlayer(
        index: Int,
        context: Context,
    ): ExoPlayer =
        playerCache[index] ?: ExoPlayer.Builder(context).build().also { player ->
            playerCache[index] = player
            if (playerCache.size > maxCacheSize) {
                val oldestIndex = playerCache.keys.first()
                playerCache.remove(oldestIndex)?.release()
            }
        }

    fun addPostTag(
        post: String,
        tag: String,
    ) {
        viewModelScope.launch {
            runCatching {
                apiService.createPostTag(mapOf("post" to post, "tag" to tag).toRequestBody())?.let {
                    appViewModel().updateVote(it, true)
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}

sealed class UiAction {
    data object Refresh : UiAction()
}

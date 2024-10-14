package com.nonio.android.ui.posts.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nonio.android.common.appViewModel
import com.nonio.android.common.showToast
import com.nonio.android.model.CommentModel
import com.nonio.android.model.CommentVoteModel
import com.nonio.android.model.LikeEvent
import com.nonio.android.model.PostModel
import com.nonio.android.model.TagEvent
import com.nonio.android.model.TagModel
import com.nonio.android.model.VoteModel
import com.nonio.android.network.NetworkHelper
import com.nonio.android.network.NetworkHelper.apiService
import com.nonio.android.network.toRequestBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PostDetailViewModel(
    private val url: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PostDetailUiState>(PostDetailUiState.Normal)

    val uiState: StateFlow<PostDetailUiState> =
        _uiState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PostDetailUiState.Normal,
            )

    companion object {
        class Factory(
            private val url: String,
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PostDetailViewModel(url) as T
        }
    }

    private val _post = MutableStateFlow<PostModel?>(null)
    val post = _post.asStateFlow()

    val comments = mutableStateListOf<CommentModel>()
    val commentVotes = mutableStateListOf<CommentVoteModel>()

    var isRefreshing by mutableStateOf(false)

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData(isShowRefresh: Boolean = true) {
        if (isShowRefresh) {
            isRefreshing = true
        }
        combine(
            flowOf(apiService.postByUrl(url)).catch {
                it.printStackTrace()
                emit(null)
            },
            flowOf(apiService.commentPost(url))
                .catch {
                    it.printStackTrace()
                    emit(null)
                }.map {
                    return@map buildNestedComments(it?.comments ?: emptyList())
                },
            flowOf(if (appViewModel().isLogin) apiService.getCommentVotes(url) else null).catch {
                it.printStackTrace()
                emit(null)
            },
        ) { postModel, commentsModel, commentVotes ->

            this.commentVotes.let {
                it.clear()
                it.addAll(commentVotes?.commentVotes ?: emptyList())
            }

            _post.value = postModel
            comments.clear()
            comments.addAll(commentsModel)
            isRefreshing = false
        }.collect()
    }

    private fun buildNestedComments(comments: List<CommentModel>): List<CommentModel> {
        val commentMap = comments.associateBy { it.id }.toMutableMap()

        val nestedComments = mutableListOf<CommentModel>()

        comments.forEach { comment ->
            if (comment.parent == null || comment.parent == 0) {
                comment.level = 1
                nestedComments.add(comment)
            } else {
                val parentComment = commentMap[comment.parent]
                if (parentComment != null) {
                    comment.level = parentComment.level + 1
                    parentComment.childList.add(comment)
                }
            }
        }
        return nestedComments
    }

    fun refresh(isShowRefresh: Boolean = true) {
        viewModelScope.launch {
            loadData(isShowRefresh)
        }
    }

    fun addComment(
        content: String,
        commentModel: CommentModel?,
        callback: () -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.emit(PostDetailUiState.ShowAddCommentLoading)
            val body =
                mapOf(
                    "post" to (commentModel?.post ?: url),
                    "parent" to commentModel?.id,
                    "content" to content,
                ).toRequestBody()

            runCatching {
                NetworkHelper.apiService.addComment(body)
                appViewModel().updateNotification()
            }.onFailure {
                it.printStackTrace()
                "add comment error".showToast()
            }.onSuccess {
                callback.invoke()
                refresh()
            }
            _uiState.emit(PostDetailUiState.Normal)
        }
    }

    fun like(
        post: PostModel,
        tagModel: TagModel,
    ) {
        viewModelScope.launch {
            runCatching {
                val requestBody = mapOf("post" to post.url, "tag" to tagModel.tag).toRequestBody()
                val isAddVote = !appViewModel().isLiked(tagModel.postID, tagModel.tagID)
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
                refresh(isShowRefresh = false)
                appViewModel().likeEventFlow.emit(
                    LikeEvent(
                        isLiked = isAddVote,
                        postID = post.id,
                        tag = tagModel.tag,
                    ),
                )
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun likeComment(commentId: Int?) {
        if (!appViewModel().isLogin || commentId == null) return
        viewModelScope.launch {
            val isVoted = commentVotes.any { it.commentId == commentId && it.upvote == true }
            runCatching {
                val requestBody = mapOf("id" to commentId, "upvoted" to !isVoted).toRequestBody()
                apiService.addCommentVote(requestBody)
                refresh(false)
            }.onFailure {
                it.printStackTrace()
            }
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
                    appViewModel().tagEventFlow.emit(TagEvent(postId = it.postID, tagId = it.tagID, tag = tag))
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}

sealed interface PostDetailUiState {
    data object ShowAddCommentLoading : PostDetailUiState

    data object Normal : PostDetailUiState
}

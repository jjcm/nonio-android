package com.nonio.android.ui.posts.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.nonio.android.common.UserHelper
import com.nonio.android.model.PostSortModel
import com.nonio.android.model.PostSortType
import com.nonio.android.model.UserCommentModel
import com.nonio.android.network.NetworkHelper
import com.nonio.android.ui.posts.UiAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class UserCommentViewModel : ViewModel() {
    init {
        viewModelScope.launch {}
    }

    private val _currentSort = MutableStateFlow(PostSortModel(sort = PostSortType.POPULAR))
    val currentSort = _currentSort.asStateFlow()

    val comments =
        Pager(config = PagingConfig(10)) {
            UserCommentsSource(
                sortProvider = {
                    currentSort.value.sort.name
                        .lowercase()
                },
                userProvider = {
                    UserHelper.getUserName()
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
}

private class UserCommentsSource(
    val sortProvider: () -> String?,
    val userProvider: () -> String,
) : PagingSource<Int, UserCommentModel>() {
    override fun getRefreshKey(state: PagingState<Int, UserCommentModel>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserCommentModel> {
        val nextPage = params.key ?: 0

        runCatching {
            val model =
                NetworkHelper.apiService.commentUser(
                    offset = nextPage,
                    sort = sortProvider(),
                    user = userProvider(),
                )
            val list = model?.comments ?: emptyList()
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

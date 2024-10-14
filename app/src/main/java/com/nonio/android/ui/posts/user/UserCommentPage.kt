package com.nonio.android.ui.posts.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.nonio.android.R
import com.nonio.android.base.BaseComposeActivity
import com.nonio.android.model.PostSortModel
import com.nonio.android.model.PostSortType
import com.nonio.android.ui.inbox.CommentItem
import com.nonio.android.ui.posts.FilterBottomSheet
import com.nonio.android.ui.posts.UiAction
import com.nonio.android.ui.theme.noRippleClickable
import kotlinx.coroutines.flow.collectLatest

class UserCommentActivity : BaseComposeActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, UserCommentActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initComposeView(): @Composable () -> Unit =
        {
            UserCommentPage()
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCommentPage(vm: UserCommentViewModel = viewModel()) {
    val context = LocalContext.current

    val comments = vm.comments.collectAsLazyPagingItems()
    val isShowBottomSheet =
        remember {
            mutableStateOf(false)
        }

    var isRefreshing by remember {
        mutableStateOf(false)
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.uiActions.collectLatest {
                when (it) {
                    UiAction.Refresh -> {
                        comments.refresh()
                    }
                }
            }
        }
    }

    val listState = rememberLazyListState()

    if (isShowBottomSheet.value) {
        FilterBottomSheet(
            isShowBottomSheet,
            PostSortModel(sort = PostSortType.POPULAR),
            isHideTop = true,
        ) { type, time ->
        }
    }

    LaunchedEffect(comments.loadState.refresh) {
        when {
            comments.loadState.refresh is LoadState.NotLoading -> {
                listState.scrollToItem(0)
                isRefreshing = false
            }

            comments.loadState.refresh is LoadState.Loading -> {
                isRefreshing = true
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        CenterAlignedTopAppBar(
            colors =
                TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary,
                ),
            navigationIcon = {
                Image(
                    modifier =
                        Modifier
                            .padding(10.dp)
                            .noRippleClickable {
                                (context as Activity).finish()
                            },
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    colorFilter =
                        ColorFilter.tint(
                            MaterialTheme.colorScheme.primary,
                            BlendMode.SrcAtop,
                        ),
                    contentDescription = null,
                )
            },
            title = {
                Text(
                    text =
                        stringResource(id = R.string.comments),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            },
            actions = {
                Image(
                    modifier =
                        Modifier
                            .padding(10.dp)
                            .size(24.dp)
                            .noRippleClickable {
                                isShowBottomSheet.value = true
                            },
                    painter = painterResource(id = R.drawable.filter),
                    contentDescription = null,
                )
            },
        )
    }) { padding ->
        PullToRefreshBox(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            isRefreshing = isRefreshing,
            onRefresh = {
                comments.refresh()
            },
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                items(
                    comments.itemCount,
                    key = comments.itemKey { it.id!! },
                ) { index ->
                    val model = comments[index]

                    CommentItem(
                        user = model?.user ?: "",
                        isRead = true,
                        upvotes = model?.upvotes ?: 0,
                        date = model?.time ?: 0,
                        title = "",
                        parentContent = "",
                        content = model?.content ?: "",
                        isLast = index == comments.itemCount - 1,
                    ) {
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewUserCommentPage() {
}

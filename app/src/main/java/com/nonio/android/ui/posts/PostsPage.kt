package com.nonio.android.ui.posts

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.nonio.android.R
import com.nonio.android.common.appViewModel
import com.nonio.android.common.getNavigatorBarHeight
import com.nonio.android.common.getScreenWidth
import com.nonio.android.common.px2dp
import com.nonio.android.model.PostSortModel
import com.nonio.android.model.PostSortType
import com.nonio.android.model.PostTimeType
import com.nonio.android.model.TagModel
import com.nonio.android.ui.posts.tag.TagActivity.Companion.startTagActivity
import com.nonio.android.ui.posts.tag.TagSearchPage
import com.nonio.android.ui.posts.tag.TagViewModel.Companion.currentTag
import com.nonio.android.ui.posts.tag.TagViewModel.Companion.updateTag
import com.nonio.android.ui.theme.AppTheme
import com.nonio.android.ui.theme.MarginHorizontalSize
import com.nonio.android.ui.theme.noRippleClickable
import com.nonio.android.ui.widget.Gap
import com.nonio.android.ui.widget.hideKeyboard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsPage(
    userName: String,
    vm: PostsViewModel =
        viewModel(
            factory =
                PostsViewModelFactory(
                    false,
                    userName = userName,
                ),
        ),
) {
    val postsState = vm.posts.collectAsLazyPagingItems()

    val tag by currentTag.collectAsState()

    var isRefreshing by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val currentPlayingIndex by vm.currentPlayingIndex.collectAsState()

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
            vm.uiActions.collectLatest {
                when (it) {
                    UiAction.Refresh -> {
                        postsState.refresh()
                    }
                }
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
        }.collect { visibleItems ->
            if (visibleItems.isNotEmpty()) {
                val firstVisibleIndex = visibleItems.first().index
                val lastVisibleIndex = visibleItems.last().index
                vm.updatePlayingIndex(
                    firstVisibleIndex,
                    lastVisibleIndex,
                    postsState.itemSnapshotList.items,
                )
            }
        }
    }

    LaunchedEffect(postsState.loadState.refresh) {
        when {
            postsState.loadState.refresh is LoadState.NotLoading -> {
                listState.scrollToItem(0)
                //  refreshState.isAnimating = false
                isRefreshing = false
            }

            postsState.loadState.refresh is LoadState.Loading -> {
                // refreshState.isRefreshing = true
                isRefreshing = true
            }
        }
    }

    val isShowBottomSheet =
        remember {
            mutableStateOf(false)
        }

    var isShowSearch by remember {
        mutableStateOf(false)
    }

    if (isShowSearch) {
        val bottomSheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { false },
            )
        ModalBottomSheet(
            sheetState = bottomSheetState,
            modifier =
                Modifier
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .fillMaxSize(),
            onDismissRequest = { isShowSearch = false },
            dragHandle = {},
        ) {
            TagSearchPage { tag ->
                scope.launch {
                    hideKeyboard(context)
                    bottomSheetState.hide()
                    isShowSearch = false
                    tag?.let {
                        updateTag(it)
                    }
                }
            }
        }
    }

    val currentSortType by vm.currentSort.collectAsState()

    if (isShowBottomSheet.value) {
        FilterBottomSheet(isShowBottomSheet, currentSortType, isHideTop = vm.isUser) { type, time ->
            vm.updateSort(type, time)
        }
    }

    LaunchedEffect(Unit) {
        appViewModel().tagEventFlow.collect { event ->

            postsState.itemSnapshotList.items
                .firstOrNull {
                    event.postId == it.id
                }?.mutableTags
                ?.add(
                    0,
                    TagModel(
                        postID = event.postId,
                        score = 1,
                        tag = event.tag,
                        tagID = event.tagId,
                        count = null,
                        isLiked = mutableStateOf(true),
                    ),
                )
        }
    }

    LaunchedEffect(Unit) {
        appViewModel().likeEventFlow.collect { event ->

            postsState.itemSnapshotList.items
                .firstOrNull {
                    it.id == event.postID
                }.let {
                    val mutableTags = it?.mutableTags
                    mutableTags?.iterator()?.let { iterator ->
                        while (iterator.hasNext()) {
                            val currentTag = iterator.next()
                            if (currentTag.tag == event.tag) {
                                currentTag.isLiked.value = event.isLiked
                                currentTag.localScore.let { score ->
                                    if (event.isLiked) {
                                        score.value += 1
                                    } else {
                                        score.value -= 1
                                    }
                                    if (score.value <= 0) {
                                        iterator.remove()
                                    }
                                }
                            }
                        }
                    }
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
                if (vm.isUser) {
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
                } else {
                    Image(
                        modifier =
                            Modifier
                                .padding(10.dp)
                                .size(24.dp)
                                .noRippleClickable {
                                    context.startTagActivity()
                                },
                        painter = painterResource(id = R.drawable.tag),
                        contentDescription = null,
                    )
                }
            },
            title = {
                Text(
                    text =

                        if (vm.isUser) {
                            stringResource(id = R.string.posts)
                        } else {
                            if (tag == null) {
                                "#All"
                            } else {
                                "#${tag?.tag}"
                            }
                        },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            },
            actions = {
                if (!vm.isUser) {
                    Image(
                        modifier =
                            Modifier
                                .padding(10.dp)
                                .size(24.dp)
                                .noRippleClickable {
                                    isShowSearch = true
                                },
                        imageVector = Icons.Filled.Search,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surfaceTint),
                        contentDescription = null,
                    )
                }

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
            modifier = Modifier.padding(padding),
            isRefreshing = isRefreshing,
            onRefresh = {
                postsState.refresh()
            },
        ) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(
                    postsState.itemCount,
                    key = postsState.itemKey { it.id!! },
                ) { index ->

                    postsState[index]?.let {
                        PostItem(
                            it,
                            isMute = true,
                            isCustomPlay = true,
                            onTapTag = {
                                scope.launch {
                                    updateTag(it)
                                }
                            },
                            onAddPostTag = { post, tag ->
                                vm.addPostTag(post, tag)
                            },
                            onLike = { postModel, tagModel ->
                                vm.like(postModel, tagModel)
                            },
                            onCreatePlayer = {
                                vm.getOrCreatePlayer(index, context).apply {
                                    val shouldAutoPlay = index == currentPlayingIndex
                                    if (shouldAutoPlay) {
                                        setMediaItem(MediaItem.fromUri(it.getMediaUrl()))
                                        prepare()
                                        playWhenReady = true
                                    } else {
                                        pause()
                                    }
                                }
                            },
                        )
                    }
                    if (index != postsState.itemCount - 1) {
                        Spacer(
                            modifier =
                                Modifier
                                    .height(8.dp)
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    isShowBottomSheet: MutableState<Boolean>,
    currentSortType: PostSortModel,
    isHideTop: Boolean,
    onUpdateSort: (PostSortType, PostTimeType?) -> Unit,
) {
    val context = LocalContext.current
    ModalBottomSheet(
        contentWindowInsets = {
            WindowInsets(
                bottom =
                    px2dp(
                        px = context.getNavigatorBarHeight().toFloat(),
                    ),
            )
        },
        // windowInsets = WindowInsets(bottom = px2dp(px = context.getNavigatorBarHeight().toFloat())),
        containerColor = MaterialTheme.colorScheme.onPrimary,
        dragHandle = {},
        modifier =
            Modifier
                .padding(horizontal = MarginHorizontalSize),
        onDismissRequest = {
            isShowBottomSheet.value = false
        },
    ) {
        // val currentSortType by vm.currentSort.collectAsState()

        var showTop by remember {
            mutableStateOf(false)
        }
        Gap(height = 10.dp)

        if (!showTop) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "Sort by...",
                color = MaterialTheme.colorScheme.secondary,
            )

            PostSortType.entries.forEachIndexed { index, sortType ->

                if (isHideTop && sortType == PostSortType.TOP) {
                    return@forEachIndexed
                }

                val updateFunc = {
                    if (index == PostSortType.entries.size - 1) {
                        showTop = true
                    } else {
                        //  vm.updateSort(sortType)
                        onUpdateSort(sortType, null)
                        isShowBottomSheet.value = false
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth()
                            .noRippleClickable(onClick = updateFunc),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = currentSortType.sort == sortType, onClick = updateFunc)

                    Text(text = sortType.type, color = MaterialTheme.colorScheme.primary)

                    Gap(width = 0.dp, modifier = Modifier.weight(1f))

                    if (index == PostSortType.entries.size - 1) {
                        Image(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }
        } else {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "Top sort timeframe",
                color = MaterialTheme.colorScheme.secondary,
            )
            PostTimeType.entries.forEachIndexed { _, time ->
                val updateFunc = {
                    //   vm.updateSort(PostSortType.TOP, timeType = time)
                    onUpdateSort(PostSortType.TOP, time)
                    isShowBottomSheet.value = false
                }
                Row(
                    modifier =
                        Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth()
                            .noRippleClickable(onClick = updateFunc),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = currentSortType.time == time, onClick = updateFunc)
                    Text(text = time.type, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun ComposeVideoPlayer(videoUrl: Uri) {
    val context = LocalContext.current

    val player =
        ExoPlayer
            .Builder(context)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(videoUrl))
                this.volume = 0f
                prepare()
            }

    DisposableEffect(key1 = player) {
        onDispose {
            Timber.d("视频 暂停")
            player.pause()
            player.release()
        }
    }

    AndroidView(
        factory = {
            (LayoutInflater.from(it).inflate(R.layout.video, null) as PlayerView).apply {
                this.controllerAutoShow = false
                this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                this.setBackgroundColor(Color.Black.toArgb())
                this.setShowNextButton(false)
                this.setShowPreviousButton(false)
                this.setShowRewindButton(false)
                this.setShowFastForwardButton(false)

                this.player = player
            }
        },
        modifier =
            Modifier
                .width(getScreenWidth().dp)
                .aspectRatio(16 / 9f),
    )
}

@Preview
@Composable
private fun PreviewLoginPage() {
    AppTheme {
        PostsPage("")
    }
}

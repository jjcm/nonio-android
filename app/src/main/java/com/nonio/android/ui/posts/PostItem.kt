package com.nonio.android.ui.posts

import QuillParser
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.nonio.android.R
import com.nonio.android.app.App
import com.nonio.android.common.appViewModel
import com.nonio.android.common.dp2px
import com.nonio.android.common.px2dp
import com.nonio.android.model.PostModel
import com.nonio.android.model.PostType
import com.nonio.android.model.TagModel
import com.nonio.android.network.Urls
import com.nonio.android.ui.account.AccountActivity
import com.nonio.android.ui.posts.detail.PostDetailActivity
import com.nonio.android.ui.posts.tag.TagSearchPage
import com.nonio.android.ui.theme.MarginHorizontalSize
import com.nonio.android.ui.theme.noRippleClickable
import com.nonio.android.ui.widget.Gap
import com.nonio.android.ui.widget.hideKeyboard
import com.nonio.android.video.RepeatMode
import com.nonio.android.video.ResizeMode
import com.nonio.android.video.VideoPlayer
import com.nonio.android.video.controller.VideoPlayerControllerConfig
import com.nonio.android.video.uri.VideoPlayerMediaItem
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostItem(
    postModel: PostModel,
    isDetail: Boolean = false,
    isLogin: Boolean = appViewModel().isLogin,
    onCreatePlayer: () -> ExoPlayer? = { App.app.getPlayer() },
    onLike: (postModel: PostModel, tagModel: TagModel) -> Unit = { _, _ -> },
    onTapTag: (tagModel: TagModel) -> Unit = {},
    onAddPostTag: (post: String, tag: String) -> Unit = { _, _ -> },
    isCustomPlay: Boolean = false,
    isMute: Boolean = false,
    autoPlay: Boolean = true,
) {
    val context = LocalContext.current
    val title = postModel.title ?: ""
    val content = postModel.content ?: ""
    val type = postModel.getPostType()
    val width = postModel.width ?: 0
    val height = postModel.height ?: 0
    val url = postModel.url ?: ""
    val link = postModel.link ?: ""
    val user = postModel.user ?: ""

    val votes = postModel.score ?: 0

    val tags = postModel.mutableTags

    val commentCount = postModel.commentCount ?: 0

    val isPreview = postModel.isPreview

    val mediaUri =
        if (!isPreview) Uri.parse(postModel.getMediaUrl()) else postModel.previewMedia ?: Uri.EMPTY

    var isShowSearch by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

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
            TagSearchPage(canCreateTag = true) { tag ->
                scope.launch {
                    hideKeyboard(context)
                    bottomSheetState.hide()
                    isShowSearch = false
                    tag?.let {
                        if (!tags.any { anyTag -> anyTag.tag == it.tag }) {
                            tags.add(
                                0,
                                it
                                    .copy(
                                        score = 1,
                                        localScore = mutableIntStateOf(1),
                                        isLiked = mutableStateOf(true),
                                    ).also { tag ->
                                        onAddPostTag(postModel.url ?: "", tag.tag ?: "")
                                    },
                            )
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (isDetail) {
                        Modifier
                    } else {
                        Modifier.clickable {
                            PostDetailActivity.start(context, url)
                        }
                    },
                ),
    ) {
        if (title.isNotBlank()) {
            Gap(height = 10.dp)
            Text(
                modifier = Modifier.padding(horizontal = MarginHorizontalSize),
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Gap(height = 10.dp)
        }

        when (type) {
            PostType.IMAGE -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                ) {
                    var imageHeight = height
                    var imageWidth = width
                    val maxHeight = 320.dp2px(context)

                    if (height > maxHeight) {
                        imageHeight = maxHeight
                        imageWidth =
                            ((width.toFloat() / height.toFloat() * maxHeight) + 0.5f).toInt()
                    }

                    AsyncImage(
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .then(
                                    if (!isPreview) {
                                        Modifier
                                            .width(px2dp(px = imageWidth.toFloat()))
                                            .height(px2dp(px = imageHeight.toFloat()))
                                    } else {
                                        Modifier
                                    },
                                ),
                        model = mediaUri,
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                    )
                }
            }

            PostType.VIDEO -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                ) {
                    VideoPlayer(
                        isCustomPlayer = isCustomPlay,
                        autoPlay = autoPlay,
                        mediaItems =
                            listOf(
//                            VideoPlayerMediaItem.RawResourceMediaItem(
//                                resourceId = R.raw.movie1,
//                            ),
//                            VideoPlayerMediaItem.AssetFileMediaItem(
//                                assetPath = "videos/test.mp4"
//                            ),
//                            VideoPlayerMediaItem.StorageMediaItem(
//                                storageUri = "content://xxxxx"
//                            ),
                                VideoPlayerMediaItem.NetworkMediaItem(
                                    url = mediaUri.toString(),
                                    //    mediaMetadata = MediaMetadata.Builder().setTitle("Widevine DASH cbcs: Tears").build(),
                                    // mimeType = MimeTypes.APPLICATION_MPD,
//                                drmConfiguration = MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
//                                    .setLicenseUri("https://proxy.uat.widevine.com/proxy?provider=widevine_test")
//                                    .build(),
                                ),
                            ),
                        resizeMode = ResizeMode.FIT,
                        handleAudioFocus = false,
                        volume = if (!isMute) 1f else 0f,
                        handleLifecycle = true,
                        usePlayerController = true,
                        enablePip = false,
                        controllerConfig =
                            VideoPlayerControllerConfig(
                                showSpeedAndPitchOverlay = false,
                                showSubtitleButton = false,
                                showCurrentTimeAndTotalTime = true,
                                showBufferingProgress = true,
                                showForwardIncrementButton = false,
                                showBackwardIncrementButton = false,
                                showBackTrackButton = false,
                                showNextTrackButton = false,
                                showRepeatModeButton = false,
                                controllerShowTimeMilliSeconds = 5_000,
                                controllerAutoShow = false,
                                showFullScreenButton = true,
                            ),
                        //    volume = 0.5f,  // volume 0.0f to 1.0f
                        repeatMode = RepeatMode.NONE, // or RepeatMode.ALL, RepeatMode.ONE
                        onCurrentTimeChanged = {
                            // long type, current player time (millisec)
                            Timber.e("CurrentTime $it")
                        },
                        playerInstance = {
                            // ExoPlayer instance (Experimental)
//                            addAnalyticsListener(
//                                object : AnalyticsListener {
//                                    // player logger
//                                }
//                            )
                        },
                        customPlayer = onCreatePlayer(),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(16 / 9f),
                        //   .align(Alignment.Center),
                    )
                }
            }

            PostType.TEXT, PostType.HTML, PostType.BLOG -> {
//                if(!isDetail){
//                    QuillParser().parseQuillJS(content).forEach {
//                        val margin = if (it.isQuote) 60.dp else 0.dp
//                        Text(
//                            modifier = Modifier.padding(start = MarginHorizontalSize + margin, end = MarginHorizontalSize),
//                            text = it.content,
//                            maxLines = 3,
//                            color = MaterialTheme.colorScheme.secondary
//                        )
//                    }
//                }
            }

            PostType.LINK -> {
                if (link.isNotBlank()) {
                    AsyncImage(
                        modifier = Modifier,
//                        .width(px2dp(px = width.toFloat()))
//                        .height(px2dp(px = height.toFloat()))
                        model = if (!isPreview) postModel.getMediaUrl() else postModel.previewMedia,
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                    )
                    Gap(height = 10.dp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .clickable {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                                }.fillMaxWidth()
                                .padding(horizontal = MarginHorizontalSize)
                                // 圆角
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp),
                                ).padding(vertical = 8.dp, horizontal = 10.dp),
                    ) {
                        Image(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(id = R.drawable.link),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        )

                        Text(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                            text = link,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                        )

                        Image(
                            modifier =
                                Modifier
                                    .width(20.dp)
                                    .height(16.dp),
                            painter = painterResource(id = R.drawable.arrow_right),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                        )
                    }
                }
            }
        }

        Gap(height = 10.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MarginHorizontalSize),
        ) {
            Row(
                modifier =
                    Modifier.noRippleClickable {
                        AccountActivity.start(context, user)
                    },
            ) {
                AsyncImage(
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .size(16.dp),
                    model = Urls.avatarImageURL(user),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
                Gap(width = 8.dp)

                Text(
                    text = user,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Gap(width = 0.dp, modifier = Modifier.weight(1f))

            Text(
                text = "$votes votes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Gap(width = 14.dp)

            Image(
                painter = painterResource(id = R.drawable.clock),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
            )
            Gap(width = 2.dp)
            Text(
                text = postModel.getFormatTime(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )

            Gap(width = 13.dp)
            Image(
                painter = painterResource(id = R.drawable.comment),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
            )
            Gap(width = 4.dp)
            Text(
                text = commentCount.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Gap(height = 10.dp)

        if (isDetail && content.isNotBlank()) {
            QuillParser().parseQuillJS(content).forEach {
                val margin = if (it.isQuote) 60.dp else 0.dp
                Text(
                    modifier =
                        Modifier.padding(
                            start = MarginHorizontalSize + margin,
                            end = MarginHorizontalSize,
                        ),
                    text = it.content,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Gap(height = 10.dp)
        }

        if (tags.isNotEmpty()) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MarginHorizontalSize),
            ) {
                LazyRow(
                    modifier =
                        Modifier
                            .weight(1f),
                ) {
                    items(tags) { tag ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                    Modifier
                                        .fillMaxHeight()
                                        .background(
                                            MaterialTheme.colorScheme.onSurface,
                                            shape =
                                                RoundedCornerShape(
                                                    topStart = 8.dp,
                                                    bottomStart = 8.dp,
                                                ),
                                        ),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                        Modifier
                                            .fillMaxHeight()
                                            .then(
                                                if (isLogin) {
                                                    Modifier.clickable {
                                                        tag.localScore.value =
                                                            if (tag.isLiked.value) tag.localScore.value - 1 else tag.localScore.value + 1
                                                        tag.isLiked.value = !tag.isLiked.value

                                                        if (tag.localScore.value <= 0) {
                                                            tags.remove(tag)
                                                        }
                                                        onLike.invoke(postModel, tag)
                                                    }
                                                } else {
                                                    Modifier
                                                },
                                            ),
                                ) {
                                    Gap(width = 6.dp)
                                    if (isLogin) {
                                        Image(
                                            modifier =
                                                Modifier
                                                    .size(16.dp),
                                            painter = painterResource(id = R.drawable.upvote),
                                            contentDescription = null,
                                            colorFilter =
                                                ColorFilter.tint(
                                                    if (tag.isLiked.value) {
                                                        MaterialTheme.colorScheme.error.copy(0.8f)
                                                    } else {
                                                        MaterialTheme.colorScheme.secondary
                                                    },
                                                ),
                                        )
                                    }
                                    Text(
                                        text = (tag.localScore.value).toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color =
                                            if (tag.isLiked.value) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.secondary
                                            },
                                        maxLines = 1,
                                    )
                                    Gap(width = 6.dp)
                                }
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier =
                                    Modifier
                                        .clickable {
                                            onTapTag.invoke(tag)
                                        }.background(
                                            MaterialTheme.colorScheme.surface,
                                            shape =
                                                RoundedCornerShape(
                                                    topEnd = 8.dp,
                                                    bottomEnd = 8.dp,
                                                ),
                                        ).fillMaxHeight()
                                        .padding(horizontal = 6.dp),
                            ) {
                                Text(
                                    text = tag.tag ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                )
                            }
                        }

                        Gap(padding = 4.dp)
                    }
                }

                Gap(width = 4.dp)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .background(
                                MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                            ).clip(shape = RoundedCornerShape(8.dp))
                            .size(28.dp, 24.dp)
                            .clickable {
                                isShowSearch = true
                            },
                ) {
                    Image(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                    )
                }
            }
            Gap(height = 10.dp)
        }
    }
}

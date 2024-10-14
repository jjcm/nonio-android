package com.nonio.android.ui.posts.detail

import QuillParser
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nonio.android.R
import com.nonio.android.base.BaseComposeActivity
import com.nonio.android.common.showToast
import com.nonio.android.model.CommentModel
import com.nonio.android.network.Urls
import com.nonio.android.ui.account.AccountActivity
import com.nonio.android.ui.posts.PostItem
import com.nonio.android.ui.posts.tag.TagViewModel
import com.nonio.android.ui.theme.MarginHorizontalSize
import com.nonio.android.ui.theme.REGULAR
import com.nonio.android.ui.theme.noRippleClickable
import com.nonio.android.ui.widget.Gap
import com.nonio.android.ui.widget.Line
import com.nonio.android.ui.widget.LoadingDialog
import com.nonio.android.ui.widget.QuillEditor
import com.nonio.android.ui.widget.QuillEditorController
import com.nonio.android.ui.widget.hideKeyboard
import kotlinx.coroutines.launch

class PostDetailActivity : BaseComposeActivity() {
    private val url: String by lazy { intent.getStringExtra("url") ?: "" }

    private val user: String? by lazy { intent.getStringExtra("user") ?: "" }

    companion object {
        fun start(
            context: Context,
            url: String,
            user: String? = null,
        ) {
            val intent = Intent(context, PostDetailActivity::class.java)
            intent.putExtra("url", url)
            if (user != null) {
                intent.putExtra("user", user)
            }
            context.startActivity(intent)
        }
    }

    override fun initComposeView(): @Composable () -> Unit =
        {
            val viewModel: PostDetailViewModel =
                viewModel(factory = PostDetailViewModel.Companion.Factory(url))
            val uiState by viewModel.uiState.collectAsState()
            PostDetailPage(viewModel, uiState)
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostDetailPage(
    vm: PostDetailViewModel,
    uiState: PostDetailUiState,
) {
    val context = LocalContext.current
    val postModel by vm.post.collectAsState()

    val scope = rememberCoroutineScope()
    val bottomSheetState =
        rememberModalBottomSheetState(
            confirmValueChange = { false },
            skipPartiallyExpanded = true,
        )

    val quillEditorController =
        remember {
            QuillEditorController()
        }

    var currentCommentModel by remember {
        mutableStateOf<CommentModel?>(null)
    }

    var isShowEditor by remember {
        mutableStateOf(false)
    }

    var isShowEditorClose by remember {
        mutableStateOf(false)
    }

    if (isShowEditorClose) {
        ModalBottomSheet(
            containerColor = Color.Transparent,
            scrimColor = Color.Transparent,
            dragHandle = {},
            onDismissRequest = {
                isShowEditorClose = false
            },
        ) {
            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                TextButton(
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = REGULAR,
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    shape = MaterialTheme.shapes.medium,
                    modifier =
                        Modifier
                            .padding(horizontal = MarginHorizontalSize)
                            .fillMaxWidth()
                            .height(44.dp),
                    onClick = {
                        isShowEditorClose = false
                        isShowEditor = false
                    },
                ) {
                    Text(
                        text = stringResource(id = R.string.delete),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                Gap(height = 8.dp)
                TextButton(
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = REGULAR,
                            contentColor = MaterialTheme.colorScheme.surfaceTint,
                        ),
                    shape = MaterialTheme.shapes.medium,
                    modifier =
                        Modifier
                            .padding(horizontal = MarginHorizontalSize)
                            .fillMaxWidth()
                            .height(44.dp),
                    onClick = {
                        isShowEditorClose = false
                    },
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }

    if (isShowEditor) {
        val configuration = LocalConfiguration.current
        val height = configuration.screenHeightDp * 0.6
        ModalBottomSheet(
            contentWindowInsets = {
                WindowInsets.ime
            },
            dragHandle = {},
            properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
            onDismissRequest = {
                isShowEditor = false
            },
            sheetState = bottomSheetState,
        ) {
            Column(
                modifier =
                    Modifier
                        .size(height.dp)
                        .background(MaterialTheme.colorScheme.onPrimary)
                        .fillMaxWidth(),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.add_comment),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.Center),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = {
                            hideKeyboard(context)
                            if (!quillEditorController.contentIsEmpty()) {
                                isShowEditorClose = true
                            } else {
                                scope
                                    .launch {
                                        bottomSheetState.hide()
                                    }.invokeOnCompletion {
                                        isShowEditor = false
                                    }
                            }
                        }) {
                            Text(
                                text = stringResource(id = R.string.cancel),
                                color = MaterialTheme.colorScheme.surfaceTint,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }

                        TextButton(onClick = {
                            hideKeyboard(context)
                            if (!quillEditorController.contentIsEmpty()) {
                                vm.addComment(
                                    quillEditorController.getContent(),
                                    commentModel = currentCommentModel,
                                ) { isShowEditor = false }
                            } else {
                                "Please input content".showToast()
                            }
                        }) {
                            Text(
                                text = stringResource(id = R.string.post),
                                color = MaterialTheme.colorScheme.surfaceTint,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }

                Line()

                QuillEditor(controller = quillEditorController)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
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
                colors =
                    TopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary,
                    ),
                title = {
                    Text(
                        text =
                            (
                                postModel?.commentCount?.toString()
                                    ?: ""
                            ) + " " + stringResource(id = R.string.comments),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                },
            )
        },
    ) {
        LoadingDialog("Loading...", uiState == PostDetailUiState.ShowAddCommentLoading)

        PullToRefreshBox(
            modifier = Modifier.padding(it),
            isRefreshing = vm.isRefreshing,
            onRefresh = {
                vm.refresh()
            },
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize(),
            ) {
                postModel?.let {
                    item {
                        PostItem(
                            postModel = it,
                            isDetail = true,
                            onLike = { postModel, tagModel ->
                                vm.like(postModel, tagModel)
                            },
                            onAddPostTag = { post: String, tag: String ->
                                vm.addPostTag(post, tag)
                            },
                            onTapTag = {
                                scope
                                    .launch {
                                        TagViewModel.updateTag(it)
                                    }.invokeOnCompletion {
                                        (context as Activity).finish()
                                    }
                            },
                        )
                    }

                    item {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(MarginHorizontalSize)
                                    .noRippleClickable {
                                        currentCommentModel = null
                                        isShowEditor = true
                                    },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.comment),
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.size(20.dp),
                            )
                            Gap(width = 8.dp)

                            Box(
                                modifier =

                                    Modifier
                                        .weight(1f)
                                        .border(
                                            shape = RoundedCornerShape(66.dp),
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                                        ),
                            ) {
                                Text(
                                    text = "Add comment",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                                    modifier =
                                        Modifier.padding(
                                            vertical = 6.dp,
                                            horizontal = MarginHorizontalSize,
                                        ),
                                )
                            }
                        }
                    }
                }

                items(vm.comments) { comment ->
                    CommentItem(comment, vm, onLiked = { commentId ->
                        vm.likeComment(commentId)
                    }) { commentModel ->
                        currentCommentModel = commentModel
                        isShowEditor = true
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: CommentModel,
    postDetailViewModel: PostDetailViewModel,
    onLiked: (commentId: Int?) -> Unit,
    onReply: (commentModel: CommentModel) -> Unit,
) {
    val context = LocalContext.current
    val expanded by remember { mutableStateOf(true) }

    val user = comment.user ?: ""
    val votes = comment.upvotes ?: 0
    val content = comment.content ?: ""

    val isVoted = postDetailViewModel.commentVotes.any { it.commentId == comment.id && it.upvote == true }

    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = {
                when (it) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        onReply.invoke(comment)
                    }

                    SwipeToDismissBoxValue.Settled -> return@rememberSwipeToDismissBoxState true
                    SwipeToDismissBoxValue.StartToEnd -> return@rememberSwipeToDismissBoxState false
                }
                return@rememberSwipeToDismissBoxState false
            },
            // positional threshold of 25%
            positionalThreshold = { it * .25f },
        )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = { DismissBackground(dismissState) },
    ) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
            Line(
                modifier =
                    Modifier.padding(
                        start = if (comment.isTopComment()) 0.dp else MarginHorizontalSize * comment.level,
                        end = if (comment.isTopComment()) 0.dp else MarginHorizontalSize,
                    ),
            )
            Gap(height = 10.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MarginHorizontalSize * comment.level,
                            end = MarginHorizontalSize,
                        ),
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

                Row(
                    modifier =
                        Modifier.noRippleClickable {
                            onLiked.invoke(comment.id)
                        },
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.upvote),
                        contentDescription = null,
                        colorFilter =
                            ColorFilter.tint(
                                if (isVoted) {
                                    MaterialTheme.colorScheme.error.copy(0.8f)
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },
                            ),
                    )
                    Gap(width = 2.dp)
                    Text(
                        text = "$votes votes",
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            if (isVoted) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                    )
                }

                Gap(width = 14.dp)

                Image(
                    painter = painterResource(id = R.drawable.clock),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                )
                Gap(width = 2.dp)
                Text(
                    text = comment.getFormatTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Gap(height = 10.dp)

            QuillParser().parseQuillJS(content).forEach {
                val margin = if (it.isQuote) 60.dp else 0.dp
                Text(
                    modifier =
                        Modifier.padding(
                            start = margin + MarginHorizontalSize * comment.level,
                            end = MarginHorizontalSize,
                        ),
                    text = it.content,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Gap(height = 10.dp)

            if (expanded) {
                comment.childList.forEach { childComment ->
                    CommentItem(comment = childComment, postDetailViewModel, onLiked = onLiked, onReply = onReply)
                }
            }
        }
    }
}

@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val color =
        when (dismissState.dismissDirection) {
            SwipeToDismissBoxValue.StartToEnd -> Color.Transparent
            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.surfaceTint
            SwipeToDismissBoxValue.Settled -> Color.Transparent
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color)
                .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Icon(
            // make sure add baseline_archive_24 resource to drawable folder
            painter = painterResource(id = R.drawable.arrow_reply),
            contentDescription = "Archive",
        )
    }
}

@Preview
@Composable
private fun PostDetailPreview() {
    PostDetailPage(vm = PostDetailViewModel(""), uiState = PostDetailUiState.Normal)
}

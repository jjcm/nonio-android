package com.nonio.android.ui.inbox

import QuillParser
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nonio.android.R
import com.nonio.android.common.appViewModel
import com.nonio.android.common.formatDuration
import com.nonio.android.model.Notification
import com.nonio.android.network.Urls
import com.nonio.android.ui.posts.detail.PostDetailActivity
import com.nonio.android.ui.theme.AppTheme
import com.nonio.android.ui.theme.MarginHorizontalSize
import com.nonio.android.ui.theme.noRippleClickable
import com.nonio.android.ui.widget.Gap
import com.nonio.android.ui.widget.Line

@Composable
fun InboxPage(vm: InboxViewModel = viewModel()) {
    LaunchedEffect(appViewModel().updateNotification) {
        vm.getNotifications(false)
    }

    LaunchedEffect(appViewModel().isLogin) {
        if (!appViewModel().isLogin) {
            vm.clearNotification()
            vm.stopRequest()
        } else {
            vm.getNotifications()
        }
    }

    InboxScreen(
        vm.refreshState,
        vm.notifications,
        onRefresh = {
            vm.getNotifications()
        },
        onMarkRead = { id -> vm.markRead(id) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    refreshState: Boolean,
    notifications: List<Notification>,
    onRefresh: () -> Unit,
    onMarkRead: (id: Int) -> Unit,
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
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
                        text = (stringResource(id = R.string.inbox)),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                },
            )
        },
    ) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize().padding(it),
            isRefreshing = refreshState,
            onRefresh = {
                onRefresh()
            },
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(notifications) { index, notification ->
                    CommentItem(
                        user = notification.user ?: "",
                        isRead = notification.read == true,
                        upvotes = notification.upvotes ?: 0,
                        date = notification.date ?: 0,
                        title = notification.post_title ?: "",
                        parentContent = notification.parent_content,
                        content = notification.content ?: "",
                        isLast = index == notifications.size - 1,
                    ) {
                        if (notification.read == false) {
                            notification.id?.let(onMarkRead)
                        }
                        PostDetailActivity.start(
                            context,
                            notification.post ?: "",
                            notification.user,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    user: String,
    isRead: Boolean,
    upvotes: Int,
    date: Long,
    title: String,
    parentContent: String?,
    content: String,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    Modifier.noRippleClickable(onClick),
                ),
    ) {
        Gap(height = 5.dp)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MarginHorizontalSize),
        ) {
            AsyncImage(
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(Color.Yellow)
                        .size(16.dp),
                model = Urls.avatarImageURL(user),
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
            Gap(width = 8.dp)

            val tips =
                remember {
                    parentContent.let {
                        if (it?.isNotEmpty() == true) {
                            " replied to your comment"
                        } else {
                            " replied to your post"
                        }
                    }
                }
            Text(
                text = user + tips,
                style = MaterialTheme.typography.bodySmall,
                color = if (isRead) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            )

            Gap(width = 0.dp, modifier = Modifier.weight(1f))

            Gap(width = 14.dp)

            Image(
                painter = painterResource(id = R.drawable.upvote),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
            )
            Gap(width = 2.dp)
            Text(
                text = upvotes.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )

            Gap(width = 13.dp)
            Image(
                painter = painterResource(id = R.drawable.clock),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
            )
            Gap(width = 4.dp)
            Text(
                text = formatDuration(date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Gap(height = 8.dp)
        Column {
            parentContent?.let { str ->
                if (str.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(horizontal = MarginHorizontalSize),
                    ) {
                        Line(width = 1.dp, height = 30.dp)

                        QuillParser()
                            .parseQuillJS(parentContent)
                            .forEach {
                                Text(
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = MarginHorizontalSize),
                                    text = it.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isRead) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                    }
                }
            }

            Gap(height = 5.dp)

            QuillParser().parseQuillJS(content).forEach {
                Text(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = MarginHorizontalSize),
                    text = it.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isRead) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Gap(height = 8.dp)
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(25.dp)
                    .padding(horizontal = MarginHorizontalSize),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(7.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Gap(width = 8.dp)
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isRead) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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

                Gap(width = 10.dp)
            }
        }

        Gap(height = 8.dp)

        if (!isLast) {
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

@Preview
@Composable
private fun PreviewLoginPage() {
    AppTheme {
        InboxScreen(
            refreshState = false,
            emptyList(),
            {},
            {},
        )
    }
}

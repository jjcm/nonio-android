package com.nonio.android.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nonio.android.R
import com.nonio.android.base.BaseComposeActivity
import com.nonio.android.common.UserHelper
import com.nonio.android.common.appViewModel
import com.nonio.android.ui.inbox.InboxPage
import com.nonio.android.ui.login.LoginPage
import com.nonio.android.ui.posts.PostsPage
import com.nonio.android.ui.settings.SettingsPage
import com.nonio.android.ui.submit.SubmitPage
import com.nonio.android.ui.theme.NoRippleInteractionSource
import com.nonio.android.ui.widget.Gap
import com.nonio.android.ui.widget.Line
import kotlinx.coroutines.launch

class MainActivity : BaseComposeActivity() {
    override fun initComposeView(): @Composable () -> Unit =
        {
            MainPage()
        }
}

private val navIcons =
    arrayOf(
        mapOf(true to R.drawable.nav_posts_selected, false to R.drawable.nav_posts),
        mapOf(true to R.drawable.nav_inbox_selected, false to R.drawable.nav_inbox),
        mapOf(true to R.drawable.nav_submit_selected, false to R.drawable.nav_submit),
        mapOf(true to R.drawable.nav_person_selected, false to R.drawable.nav_person),
        mapOf(true to R.drawable.nav_settings_selected, false to R.drawable.nav_settings),
    )

private val navTitles =
    arrayOf(
        R.string.posts,
        R.string.inbox,
        R.string.submit,
        R.string.login,
        R.string.settings,
    )

@Composable
fun MainPage() {
    val isLogin = appViewModel().isLogin
    val notificationCount = appViewModel().notificationCount
    val userAvatar = UserHelper.getUserAvatar()
    val userName = UserHelper.getUserName()

    MainPageScreen(
        isLogin = isLogin,
        notificationCount = notificationCount,
        userAvatar = userAvatar,
        userName = userName,
        postPage = { PostsPage(userName) },
        inboxPage = { InboxPage() },
        submitPage = { SubmitPage() },
        loginPage = { LoginPage() },
        settingsPage = { SettingsPage() },
    )
}

@Composable
fun MainPageScreen(
    isLogin: Boolean,
    notificationCount: Int,
    userName: String? = null,
    userAvatar: String? = null,
    postPage: @Composable () -> Unit,
    inboxPage: @Composable () -> Unit,
    submitPage: @Composable () -> Unit,
    loginPage: @Composable () -> Unit,
    settingsPage: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val pagerState = rememberPagerState { navIcons.size }

    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()

    Column {
        HorizontalPager(
            modifier =
                Modifier
                    .weight(1f),
            state = pagerState,
            contentPadding = PaddingValues(0.dp),
            userScrollEnabled = false,
            beyondViewportPageCount = 5,
            //  outOfBoundsPageCount = 5
            // beyondBoundsPageCount = 5
        ) { pageIndex ->

            when (navTitles[pageIndex]) {
                R.string.posts -> {
                    postPage()
                }

                R.string.inbox -> {
                    inboxPage()
                }

                R.string.submit -> {
                    submitPage()
                }

                R.string.login -> {
                    loginPage()
                }

                R.string.settings -> {
                    settingsPage()
                }
            }
        }
        Line()

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(color = colorScheme.onPrimary)
                    .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                    .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for ((index, map) in navIcons.withIndex()) {
                val isSelected = selectedIndex == index
                val focusManager = LocalFocusManager.current
                NavigationBarItem(
                    interactionSource = NoRippleInteractionSource,
                    colors =
                        NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Transparent,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.Transparent,
                        ),
                    selected = isSelected,
                    onClick = {
                        selectedIndex = index
                        scope.launch {
                            pagerState.scrollToPage(index)
                            // Prevent repeated pop-up of the keyboard
                            if (index != 2) {
                                focusManager.clearFocus()
                            }
                        }
                    },
                    icon = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (index == 3 && isLogin) {
                                AsyncImage(
                                    modifier =
                                        Modifier
                                            .clip(CircleShape)
                                            .size(24.dp)
                                            .then(
                                                if (!isSelected) {
                                                    Modifier
                                                } else {
                                                    Modifier.border(
                                                        2.dp,
                                                        color = colorScheme.surfaceTint,
                                                        shape = CircleShape,
                                                    )
                                                },
                                            ),
                                    model = userAvatar,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                )
                            } else if (index == 1 && isLogin) {
                                BadgedBox(
                                    badge = {
                                        if (notificationCount != 0) {
                                            Badge { Text(notificationCount.toString()) }
                                        }
                                    },
                                ) {
                                    Image(
                                        painter = painterResource(id = map[isSelected]!!),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                            } else {
                                Image(
                                    painter = painterResource(id = map[isSelected]!!),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                )
                            }

                            Gap(height = 6.dp)

                            val title =
                                if (index == 3 && isLogin) {
                                    userName ?: ""
                                } else {
                                    stringResource(
                                        id = navTitles[index],
                                    )
                                }

                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = typography.labelSmall,
                                color = if (isSelected) colorScheme.surfaceTint else colorScheme.secondary,
                            )
                        }
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    MainPageScreen(
        false,
        2,
        "kale",
        "https://avatars.githubusercontent.com/u/11699668?v=4",
        {},
        {},
        {},
        {},
        {},
    )
}

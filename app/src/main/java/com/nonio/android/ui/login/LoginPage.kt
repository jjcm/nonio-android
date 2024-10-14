package com.nonio.android.ui.login

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.util.PatternsCompat.EMAIL_ADDRESS
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nonio.android.R
import com.nonio.android.common.UserHelper
import com.nonio.android.common.appViewModel
import com.nonio.android.common.showToast
import com.nonio.android.model.UserInfoModel
import com.nonio.android.network.Urls
import com.nonio.android.ui.posts.user.UserPostActivity
import com.nonio.android.ui.theme.AppTheme
import com.nonio.android.ui.theme.MarginHorizontalSize
import com.nonio.android.ui.theme.noRippleClickable
import com.nonio.android.ui.widget.Gap
import com.nonio.android.ui.widget.LoadingDialog

private fun getUserAvatar(): Any {
    if (UserHelper.isLogin()) {
        return UserHelper.getUserAvatar()
    }
    return R.drawable.avatar
}

@Composable
fun LoginPage(vm: LoginViewModel = viewModel()) {
    val loginUiState by vm.loginUiState.collectAsState()
    val email by vm.email.collectAsState()
    val pwd by vm.pwd.collectAsState()

    val userInfo by vm.userInfo.collectAsState()

    LoginScreen(
        isLogin = appViewModel().isLogin,
        uiState = loginUiState,
        email = email,
        pwd = pwd,
        userInfo = userInfo,
        onEmailChange = vm::onEmailChange,
        onPwdChange = vm::onPwdChange,
        toLogin = vm::login,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreen(
    isLogin: Boolean,
    uiState: LoginUiState,
    email: String,
    pwd: String,
    userInfo: UserInfoModel?,
    onEmailChange: (String) -> Unit,
    onPwdChange: (String) -> Unit,
    toLogin: () -> Unit,
) {
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
            title = {
                Text(
                    text = stringResource(R.string.account),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            },
        )
    }) { paddingValues ->

        LoadingDialog("Login...", uiState == LoginUiState.Loading)

        var isShow by remember {
            mutableStateOf(false)
        }
        LaunchedEffect(uiState) {
            if (uiState is LoginUiState.UserInvalid) {
                isShow = true
            }
        }
        if (isShow) {
            UserInvalidDialog(onDismissRequest = { isShow = false }, toLogin = toLogin)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .padding(paddingValues)
                    .padding(MarginHorizontalSize),
        ) {
            Gap(height = 15.dp)

            AsyncImage(
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .size(62.dp),
                model =
                    getUserAvatar(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            if (isLogin) {
                Gap(height = 10.dp)
                Text(
                    text = UserHelper.getUserName(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (isLogin) {
                HasLogin(
                    userInfo?.posts,
                    userInfo?.comments,
                    userInfo?.postKarma,
                    userInfo?.commentKarma,
                    userName = UserHelper.getUserName(),
                )
            } else {
                NotLogin(email, pwd, onEmailChange, onPwdChange, toLogin)
            }
        }
    }
}

@Composable
private fun UserInvalidDialog(
    onDismissRequest: () -> Unit = {},
    toLogin: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.incorrect_login),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.login_incorrect_tips),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        },
        dismissButton = {
            FilledTonalButton(
                onClick = {
                    val uri = Uri.parse(Urls.REGISTER_WEB)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                },
                colors =
                    ButtonDefaults
                        .filledTonalButtonColors()
                        .copy(containerColor = MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(11.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.create_account),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.surfaceTint,
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    onDismissRequest()
                    toLogin()
                },
                colors =
                    ButtonDefaults
                        .filledTonalButtonColors()
                        .copy(containerColor = MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(11.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.try_again),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.surfaceTint,
                )
            }
        },
    )
}

@Composable
fun ColumnScope.HasLogin(
    posts: Int?,
    comments: Int?,
    postKarma: Int?,
    commentKarma: Int?,
    userName: String,
    showLogout: Boolean = true,
) {
    val context = LocalContext.current

    Gap(height = 20.dp)

    Text(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = MarginHorizontalSize),
        text = stringResource(R.string.content).uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
    )

    Gap(height = 7.dp)

    TileItem(
        R.drawable.posts,
        stringResource(id = R.string.posts),
        posts?.toString() ?: "",
        RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        onClick = {
            UserPostActivity.start(context = context, userName)
        },
        showArrow = true,
    )

    Box(
        modifier =
            Modifier
                .height(0.5.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .background(MaterialTheme.colorScheme.secondary),
    )

    TileItem(
        R.drawable.comment,
        stringResource(R.string.comments),
        comments?.toString() ?: "",
        RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
        onClick = {
            // todo to user comment
            //  UserCommentActivity.start(context = context)
        },
        showArrow = false,
    )

    Gap(height = 24.dp)

    Text(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = MarginHorizontalSize),
        text = stringResource(R.string.stats),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
    )

    Gap(height = 7.dp)

    TileItem(
        R.drawable.posts,
        stringResource(R.string.post_karma),
        postKarma?.toString() ?: "",
        RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
    )

    Box(
        modifier =
            Modifier
                .height(0.5.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .background(MaterialTheme.colorScheme.secondary),
    )

    TileItem(
        R.drawable.comment,
        stringResource(R.string.comment_karma),
        commentKarma?.toString() ?: "",
        RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
    )

    Gap(height = 24.dp)

    if (showLogout) {
        FilledTonalButton(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(11.dp),
            colors =
                ButtonDefaults
                    .filledTonalButtonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.surface),
            onClick = {
                UserHelper.logout()
            },
        ) {
            Text(
                text = stringResource(R.string.logout),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun TileItem(
    imageRes: Int,
    title: String,
    count: String,
    roundedCornerShape: RoundedCornerShape,
    showArrow: Boolean = false,
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .noRippleClickable(onClick = onClick)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = roundedCornerShape,
                ).padding(
                    horizontal = 20.dp,
                    vertical = 12.dp,
                ),
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            contentScale = ContentScale.Fit,
        )
        Gap(width = 16.dp)

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Gap(width = 0.dp, modifier = Modifier.weight(1f))

        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
        )

        Gap(width = 14.dp)

        if (showArrow) {
            Image(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
            )
        }
    }
}

@Composable
private fun ColumnScope.NotLogin(
    email: String,
    pwd: String,
    onEmailChange: (String) -> Unit,
    onPwdChange: (String) -> Unit,
    toLogin: () -> Unit,
) {
    Gap(height = 47.dp)
    Text(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = MarginHorizontalSize),
        text = stringResource(R.string.credentials),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
    )

    Gap(height = 7.dp)

    TextInput(false, email, onEmailChange)

    Box(
        modifier =
            Modifier
                .height(0.5.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = MarginHorizontalSize)
                .background(MaterialTheme.colorScheme.secondary),
    )
    TextInput(true, pwd, onPwdChange)

    Gap(height = 24.dp)

    FilledTonalButton(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(11.dp),
        colors =
            ButtonDefaults
                .filledTonalButtonColors()
                .copy(containerColor = MaterialTheme.colorScheme.surface),
        onClick = {
            when {
                email.isEmpty() -> {
                    "please input email".showToast()
                }
                !EMAIL_ADDRESS.matcher(email).matches() -> {
                    "please input correct email".showToast()
                }

                pwd.isEmpty() -> {
                    "please input password".showToast()
                }

                else -> {
                    toLogin()
                }
            }
        },
    ) {
        Text(
            text = stringResource(id = R.string.login),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.surfaceTint,
        )
    }
}

@Composable
fun TextInput(
    isPwd: Boolean,
    value: String,
    onInputChange: (String) -> Unit,
) {
    TextField(
        maxLines = 1,
        keyboardOptions =
            if (!isPwd) {
                KeyboardOptions(imeAction = ImeAction.Next)
            } else {
                KeyboardOptions(
                    imeAction = ImeAction.Done,
                )
            },
        visualTransformation =
            if (!isPwd) VisualTransformation.None else PasswordVisualTransformation(),
        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.primary),
        colors =
            TextFieldDefaults.colors().copy(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
            ),
        shape =
            RoundedCornerShape(
                topEnd = if (isPwd) 0.dp else 10.dp,
                topStart = if (isPwd) 0.dp else 10.dp,
                bottomEnd = if (!isPwd) 0.dp else 10.dp,
                bottomStart = if (!isPwd) 0.dp else 10.dp,
            ),
        value = value,
        onValueChange = onInputChange,
        leadingIcon = {
            Text(
                modifier =
                    Modifier
                        .padding(start = 16.dp)
                        .width(width = 100.dp),
                text = if (!isPwd) stringResource(R.string.email) else stringResource(R.string.password),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier =
                        Modifier
                            .padding(2.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable {
                                onInputChange("")
                            },
                ) {
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(),
    )
}

@Preview
@Composable
private fun PreviewLoginPage() {
    AppTheme {
        LoginScreen(false, LoginUiState.NoLogin, "", "", UserInfoModel(), {}, {}, {})
    }
}

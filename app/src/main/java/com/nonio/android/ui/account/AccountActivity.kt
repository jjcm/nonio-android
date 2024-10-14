package com.nonio.android.ui.account

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nonio.android.R
import com.nonio.android.base.BaseComposeActivity
import com.nonio.android.network.Urls
import com.nonio.android.ui.login.HasLogin
import com.nonio.android.ui.theme.MarginHorizontalSize
import com.nonio.android.ui.theme.noRippleClickable
import com.nonio.android.ui.widget.Gap

class AccountActivity : BaseComposeActivity() {
    companion object {
        fun start(
            context: Context,
            user: String,
        ) {
            context.startActivity(
                Intent(context, AccountActivity::class.java).apply {
                    putExtra("user", user)
                },
            )
        }
    }

    override fun initComposeView(): @Composable () -> Unit =
        {
            val user =
                remember {
                    intent.getStringExtra("user")
                }

            AccountPage(modifier = Modifier.fillMaxSize(), userName = user ?: "")
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPage(
    modifier: Modifier = Modifier,
    userName: String,
    viewModel: AccountViewModel = viewModel(factory = AccountViewModelFactory(userName)),
) {
    val userInfo by viewModel.userInfo.collectAsState()

    val context = LocalContext.current
    Scaffold(modifier = modifier, topBar = {
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
                    text = stringResource(R.string.account),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            },
        )
    }) { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxSize()
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
                    Urls.avatarImageURL(userName),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            Gap(height = 10.dp)
            Text(
                text = userName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            userInfo?.let {
                HasLogin(
                    userInfo?.posts,
                    userInfo?.comments,
                    userInfo?.postKarma,
                    userInfo?.commentKarma,
                    userName,
                    showLogout = false,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewAccountPage() {
    AccountPage(userName = "username")
}

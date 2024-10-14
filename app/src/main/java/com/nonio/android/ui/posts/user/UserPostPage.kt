package com.nonio.android.ui.posts.user

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nonio.android.base.BaseComposeActivity
import com.nonio.android.ui.posts.PostsPage
import com.nonio.android.ui.posts.PostsViewModelFactory

class UserPostActivity : BaseComposeActivity() {
    companion object {
        fun start(
            context: Context,
            userName: String,
        ) {
            val intent =
                Intent(context, UserPostActivity::class.java).apply {
                    putExtra("user", userName)
                }
            context.startActivity(intent)
        }
    }

    override fun initComposeView(): @Composable () -> Unit =
        {
            val userName = remember { intent.getStringExtra("user") ?: "" }
            PostsPage(userName, vm = viewModel(factory = PostsViewModelFactory(userName = userName, isUser = true)))
        }
}

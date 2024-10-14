package com.nonio.android.ui.posts.tag

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nonio.android.R
import com.nonio.android.base.BaseComposeActivity
import com.nonio.android.ui.posts.tag.TagViewModel.Companion.updateTag
import com.nonio.android.ui.theme.MarginHorizontalSize
import com.nonio.android.ui.theme.noRippleClickable
import com.nonio.android.ui.widget.Gap
import com.nonio.android.ui.widget.Line
import kotlinx.coroutines.launch

class TagActivity : BaseComposeActivity() {
    companion object {
        fun Context.startTagActivity() {
            startActivity(Intent(this, TagActivity::class.java))
        }
    }

    override fun initComposeView(): @Composable () -> Unit = { TagPage() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagPage(vm: TagViewModel = viewModel()) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    Scaffold(topBar = {
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
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary, BlendMode.SrcAtop),
                    contentDescription = null,
                )
            },
            title = {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleMedium,
                )
            },
        )
    }) {
        val tags by vm.tags.collectAsState()
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(it),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MarginHorizontalSize)
                        .noRippleClickable {
                            scope.launch {
                                updateTag(null)
                            }
                            (context as Activity).finish()
                        },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.house),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )

                Gap(width = 8.dp)
                Text(
                    text = "All Posts",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .padding(vertical = 11.dp),
                )
            }

            Text(
                modifier = Modifier.padding(horizontal = MarginHorizontalSize, vertical = 4.dp),
                text = "ALL TAGS",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                tags?.tags?.let { tags ->

                    items(tags, key = { it.tag ?: "" }) { tag ->

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = MarginHorizontalSize,
                                        vertical = 11.dp,
                                    ).noRippleClickable {
                                        scope.launch {
                                            updateTag(tag)
                                        }
                                        (context as Activity).finish()
                                    },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.hash),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )

                            Gap(width = 8.dp)
                            Text(
                                text = tag.tag ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Line()
                    }
                }
            }
        }
    }
}

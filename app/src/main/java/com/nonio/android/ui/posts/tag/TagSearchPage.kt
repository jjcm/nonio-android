package com.nonio.android.ui.posts.tag

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nonio.android.R
import com.nonio.android.model.TagModel
import com.nonio.android.model.TagsModel
import com.nonio.android.ui.theme.AppTheme
import com.nonio.android.ui.theme.MarginHorizontalSize
import com.nonio.android.ui.widget.Gap
import com.nonio.android.ui.widget.Line

@Composable
fun TagSearchPage(
    canCreateTag: Boolean = false,
    onClose: (TagModel?) -> Unit,
) {
    val viewModel: TagSearchViewModel = viewModel()
    val tags by viewModel.tags.collectAsState()

    TagSearchScreen(viewModel, tags, canCreateTag) {
        onClose(it)
        viewModel.clear()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSearchScreen(
    viewModel: TagSearchViewModel,
    tagsModel: TagsModel?,
    canCreateTag: Boolean = false,
    onClose: (TagModel?) -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.onSecondary, topBar = {
        CenterAlignedTopAppBar(
            windowInsets = WindowInsets(top = 0),
            colors =
                TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onSecondary,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary,
                ),
            title = {
                Text(
                    text = (stringResource(id = R.string.search_tags)),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            actions = {
                TextButton(onClick = { onClose(null) }) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.surfaceTint,
                    )
                }
            },
        )
    }) { paddingValues ->

        var inputText by remember {
            mutableStateOf("")
        }
        Column(modifier = Modifier.padding(paddingValues)) {
            TextInputRow(inputText) {
                inputText = it
                viewModel.search(it)
            }
            Gap(height = 20.dp)

            if ((canCreateTag && inputText.isNotBlank()) || !tagsModel?.tags.isNullOrEmpty()) {
                LazyColumn(
                    modifier =
                        Modifier
                            .padding(horizontal = MarginHorizontalSize)
                            .background(MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(16.dp))
                            .weight(1f)
                            .fillMaxWidth(),
                ) {
                    val keyword = inputText

                    if (canCreateTag) {
                        item {
                            Column(
                                modifier =
                                    Modifier.clickable {
                                        if (keyword.isEmpty()) return@clickable
                                        onClose(
                                            TagModel(
                                                tag = keyword,
                                                postID = null,
                                                score = null,
                                                tagID = null,
                                                count = null,
                                            ),
                                        )
                                    },
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        modifier =
                                            Modifier.padding(
                                                vertical = 12.dp,
                                                horizontal = MarginHorizontalSize,
                                            ),
                                        style =
                                            MaterialTheme.typography.bodyLarge.copy(
                                                MaterialTheme.colorScheme.primary,
                                            ),
                                        text = keyword,
                                    )

                                    Text(
                                        modifier =
                                            Modifier.padding(
                                                vertical = 12.dp,
                                                horizontal = MarginHorizontalSize,
                                            ),
                                        style =
                                            MaterialTheme.typography.bodyLarge.copy(
                                                MaterialTheme.colorScheme.surfaceTint,
                                            ),
                                        text = stringResource(R.string.create_new_tag),
                                    )
                                }

                                Line(modifier = Modifier.padding(horizontal = MarginHorizontalSize))
                            }
                        }
                    }

                    items(tagsModel?.tags?.size ?: 0) { index ->
                        tagsModel?.tags?.get(index)?.let { model ->
                            Column(
                                modifier =
                                    Modifier.clickable {
                                        onClose(model)
                                    },
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        modifier =
                                            Modifier.padding(
                                                vertical = 12.dp,
                                                horizontal = MarginHorizontalSize,
                                            ),
                                        style = MaterialTheme.typography.bodyLarge,
                                        text =
                                            getHighlightAnnotatedString(
                                                text = model.tag ?: "",
                                                keyword = keyword,
                                                keywordColor = MaterialTheme.colorScheme.primary,
                                                defaultColor = MaterialTheme.colorScheme.secondary,
                                            ),
                                    )

                                    Text(
                                        text = model.count?.toString() ?: "",
                                        modifier =
                                            Modifier.padding(
                                                vertical = 12.dp,
                                                horizontal = MarginHorizontalSize,
                                            ),
                                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                                    )
                                }
                                if (index != (tagsModel.tags.size) - 1) {
                                    Line(modifier = Modifier.padding(horizontal = MarginHorizontalSize))
                                }
                            }
                        }
                    }
                }

                Gap(height = 20.dp)
            }
        }
    }
}

@Composable
private fun TextInputRow(
    value: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        cursorBrush =
            SolidColor(
                MaterialTheme.colorScheme.primary,
            ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onSecondary)
                        .padding(horizontal = MarginHorizontalSize)
                        .padding(bottom = 10.dp)
                        .background(
                            MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(8.dp),
                        ).padding(top = 2.dp)
                        .padding(vertical = 6.dp, horizontal = MarginHorizontalSize),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.secondary),
                )
                Gap(width = 6.dp)
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isBlank()) {
                        Text(
                            text = "Search...",
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        )
                    }
                    innerTextField()
                }

                Image(
                    modifier =
                        Modifier
                            .alpha(
                                if (value.isNotBlank()) 1f else 0f,
                            ).clickable {
                                onValueChange("")
                            },
                    painter = painterResource(id = R.drawable.error),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                )
            }
        },
    )
}

private fun getHighlightAnnotatedString(
    text: String,
    keyword: String,
    keywordColor: Color,
    defaultColor: Color,
    ignoreCase: Boolean = true,
): AnnotatedString {
    val annotatedString =
        buildAnnotatedString {
            if (keyword.isEmpty()) {
                withStyle(style = SpanStyle(color = defaultColor)) {
                    append(text)
                }
                return@buildAnnotatedString
            }

            var startIndex = 0
            val textToSearch = if (ignoreCase) text.lowercase() else text
            val keywordToSearch = if (ignoreCase) keyword.lowercase() else keyword

            while (startIndex < text.length) {
                val index = textToSearch.indexOf(keywordToSearch, startIndex)
                if (index == -1) {
                    withStyle(style = SpanStyle(color = defaultColor)) {
                        append(text.substring(startIndex))
                    }
                    break
                }
                if (index > startIndex) {
                    withStyle(style = SpanStyle(color = defaultColor)) {
                        append(text.substring(startIndex, index))
                    }
                }
                withStyle(style = SpanStyle(color = keywordColor, fontWeight = FontWeight.W600)) {
                    append(text.substring(index, index + keyword.length))
                }
                startIndex = index + keyword.length
            }
        }
    return annotatedString
}

@Preview
@Composable
private fun PreviewLoginPage() {
    AppTheme {
        TagSearchScreen(
            TagSearchViewModel(),
            TagsModel(listOf(TagModel("", 0, "1111", "", 0))),
            false,
            {},
        )
    }
}

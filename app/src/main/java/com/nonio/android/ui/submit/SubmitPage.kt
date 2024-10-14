@file:OptIn(ExperimentalMaterial3Api::class)

package com.nonio.android.ui.submit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nonio.android.R
import com.nonio.android.app.App
import com.nonio.android.common.MediaTypeUtil
import com.nonio.android.common.MediaTypeUtil.MediaType.*
import com.nonio.android.model.LinkModel
import com.nonio.android.model.PostCreateParam
import com.nonio.android.model.PostModel
import com.nonio.android.model.PostType
import com.nonio.android.model.Resolution
import com.nonio.android.model.TagModel
import com.nonio.android.model.getDisplayName
import com.nonio.android.model.getType
import com.nonio.android.network.Urls
import com.nonio.android.ui.posts.ComposeVideoPlayer
import com.nonio.android.ui.posts.PostItem
import com.nonio.android.ui.posts.detail.PostDetailActivity
import com.nonio.android.ui.posts.tag.TagSearchPage
import com.nonio.android.ui.theme.AppTheme
import com.nonio.android.ui.theme.MarginHorizontalSize
import com.nonio.android.ui.theme.NoRippleInteractionSource
import com.nonio.android.ui.widget.Gap
import com.nonio.android.ui.widget.Line
import com.nonio.android.ui.widget.LoadingDialog
import com.nonio.android.ui.widget.hideKeyboard
import convertStringToQuillJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Composable
fun SubmitPage() {
    val viewModel: SubmitViewModel = viewModel()
    val linkModel by viewModel.linkModel.collectAsState()

    val postUrlUiStateState by viewModel.postUrlUiState.collectAsState()

    val uploadingMedia by viewModel.uploadingMedia.collectAsState()

    val resolutions by viewModel.resolutions.collectAsState()

    val encodingMedia by viewModel.encodingMedia.collectAsState()
    val submitting by viewModel.submitting.collectAsState()

    SubmitScreen(
        viewModel = viewModel,
        linkModel,
        postUrlUiStateState,
        uploadingMedia,
        resolutions,
        encodingMedia,
        submitting,
    )
}

@Composable
fun SubmitScreen(
    viewModel: SubmitViewModel,
    linkModel: LinkModel? = null,
    postUrlUiState: PostUrlUiState,
    uploadingMedia: Boolean,
    resolutionProgressList: List<ResolutionProgress>,
    encodingMedia: Boolean,
    submitting: Boolean,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var selectIndex by remember { mutableIntStateOf(0) }
    val titles =
        listOf(
            R.string.link,
            R.string.media,
            R.string.text,
        )

    var link by remember {
        mutableStateOf("")
    }

    var title by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var postUrl by remember {
        mutableStateOf("")
    }

    var mediaUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var type by remember {
        mutableStateOf(PostType.LINK)
    }

    var isShowSearch by remember {
        mutableStateOf(false)
    }

    val tags =
        remember {
            mutableStateListOf<TagModel>()
        }

    val pickMedia =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
            viewModel.viewModelScope.launch {
                delay(100)
                App.app.getPlayer().pause()
            }
            if (uri != null) {
                Timber.d("PhotoPicker Selected URI: $uri")
                mediaUri = null
                type =
                    when (selectIndex) {
                        0 -> PostType.LINK
                        1 -> {
                            val mediaType =
                                when (
                                    MediaTypeUtil.getMediaType(
                                        context = context,
                                        uri,
                                    )
                                ) {
                                    VIDEO -> {
                                        viewModel.uploadMedia(
                                            context = context,
                                            uri,
                                            PostType.VIDEO,
                                        ) {
                                        }
                                        mediaUri = uri
                                        PostType.VIDEO
                                    }

                                    else -> {
                                        viewModel.uploadMedia(
                                            context = context,
                                            uri,
                                            PostType.IMAGE,
                                        ) {
                                        }
                                        mediaUri = uri
                                        PostType.IMAGE
                                    }
                                }
                            mediaType
                        }

                        else -> PostType.TEXT
                    }
            } else {
                Timber.d("PhotoPicker No media selected")
            }
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
            TagSearchPage(canCreateTag = true) { tag ->
                scope.launch {
                    hideKeyboard(context)
                    bottomSheetState.hide()
                    isShowSearch = false
                    tag?.let {
                        tags.add(it)
                    }
                }
            }
        }
    }

    if (submitting) {
        LoadingDialog(stringResource(R.string.submitting), submitting)
    }

    LaunchedEffect(postUrl) {
        viewModel.checkPostUrl(postUrl)
    }

    LaunchedEffect(linkModel) {
        linkModel?.let {
            title = it.title ?: ""
            description = it.description ?: ""
            mediaUri = Uri.parse(it.image)
            if (postUrl.isBlank()) {
                val replacedSpaces = title.replace(" ", "-")
                val cleanedString = replacedSpaces.replace(Regex("[^A-Za-z0-9-]"), "")
                if (cleanedString.isNotBlank()) {
                    postUrl = cleanedString.lowercase()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
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
                        text = (stringResource(id = R.string.submit_post)),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                },
            )
        },
    ) { paddingValues ->

        val nestedScrollConnection =
            remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource,
                    ): Offset {
                        val delta = available.y
                        keyboardController?.hide()
                        return Offset.Zero
                    }
                }
            }

        Column(
            Modifier
                .fillMaxSize()
                .nestedScroll(connection = nestedScrollConnection)
                .pointerInput(Unit) {
                    detectTapGestures {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                }.verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = MarginHorizontalSize),
        ) {
            Gap(height = 16.dp)
            TabRow(
                modifier =
                    Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(9.dp)),
                containerColor = MaterialTheme.colorScheme.tertiary.copy(0.12f),
                contentColor = MaterialTheme.colorScheme.primary,
                selectedTabIndex = selectIndex,
                divider = {},
                indicator = { tabPositions ->
                    Box(
                        modifier =
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectIndex])
                                .height(3.dp)
                                .padding(horizontal = 40.dp)
                                .clip(RoundedCornerShape(8.dp)) // clip modifier not working
                                .background(color = MaterialTheme.colorScheme.primary),
                    )
                },
            ) {
                titles.forEachIndexed { index, title ->

                    Tab(
                        modifier = Modifier.clip(RoundedCornerShape(9.dp)),
                        selected = selectIndex == index,
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.primary,
                        interactionSource = NoRippleInteractionSource,
                        onClick = {
                            selectIndex = index
                            type =
                                when (index) {
                                    0 -> PostType.LINK
                                    1 -> {
                                        when (
                                            mediaUri?.let {
                                                val mediaType =
                                                    MediaTypeUtil.getMediaType(context = context, it)
                                                mediaType
                                            }
                                        ) {
                                            VIDEO -> PostType.VIDEO
                                            else -> PostType.IMAGE
                                        }
                                    }

                                    else -> PostType.TEXT
                                }
                        },
                        text = {
                            Text(
                                text = stringResource(id = title),
                                maxLines = 2,
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }
            Gap(height = 16.dp)

            when (selectIndex) {
                0 -> {
                    Text(
                        text = stringResource(R.string.details),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Gap(height = 7.dp)

                    Column(
                        modifier =
                            Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    shape = RoundedCornerShape(10.dp),
                                ),
                    ) {
                        MyTextField(
                            stringResource(id = R.string.link),
                            link,
                            hint = stringResource(R.string.example_url),
                            isError = !viewModel.isValidUrl(link),
                            errorMessage = "Invalid URL",
                        ) {
                            link = it
                            viewModel.parseUrl(link)
                        }
                        Line(modifier = Modifier.padding(start = 16.dp))
                        MyTextField(
                            stringResource(R.string.title),
                            title,
                            hint = stringResource(R.string.value),
                        ) { title = it }
                    }

                    Gap(height = 16.dp)

                    MyTextField(
                        stringResource(R.string.description),
                        description,
                        hint = stringResource(R.string.description_hint),
                        maxLine = 3,
                    ) { description = it }
                }

                1 -> {
                    Text(
                        text =
                            if (encodingMedia) {
                                stringResource(R.string.transcoding)
                            } else {
                                stringResource(
                                    R.string.upload_media,
                                )
                            },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Gap(height = 7.dp)

                    if (uploadingMedia) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            LinearProgressIndicator(
                                strokeCap = StrokeCap.Square,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(4.dp),
                                gapSize = 0.dp,
                                color = MaterialTheme.colorScheme.surfaceTint,
                                trackColor = MaterialTheme.colorScheme.secondary.copy(0.16f),
                            )
                        }
                        Gap(height = 16.dp)
                    } else {
                        if (mediaUri != null && type == PostType.IMAGE) {
                            AsyncImage(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(227.dp),
                                model = mediaUri,
                                contentScale = ContentScale.Crop,
                                contentDescription = null,
                            )
                            Gap(height = 16.dp)
                        } else if (type == PostType.VIDEO) {
                            if (encodingMedia) {
                                ResolutionProgressView(resolutionProgressList)
                            } else {
                                mediaUri?.let {
                                    ComposeVideoPlayer(videoUrl = it)
                                }
                            }
                            Gap(height = 16.dp)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .clickable {
                                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                                }.fillMaxWidth()
                                .height(44.dp)
                                .background(
                                    MaterialTheme.colorScheme.onPrimary,
                                    shape = RoundedCornerShape(10.dp),
                                ).padding(horizontal = MarginHorizontalSize),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.add_media),
                            contentDescription = null,
                        )
                        Gap(width = 16.dp)
                        Text(
                            text = stringResource(R.string.select_media),
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                        )
                    }

                    Gap(height = 16.dp)

                    Text(
                        text = stringResource(R.string.details),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Gap(height = 7.dp)

                    MyTextField(
                        stringResource(R.string.title),
                        title,
                        hint = stringResource(R.string.value),
                        maxLine = 1,
                    ) { title = it }
                    Gap(height = 16.dp)

                    MyTextField(
                        stringResource(R.string.description),
                        description,
                        hint = stringResource(R.string.description_hint),
                        maxLine = 3,
                    ) { description = it }
                }

                2 -> {
                    Text(
                        text = stringResource(R.string.details),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Gap(height = 7.dp)

                    MyTextField(
                        stringResource(R.string.title),
                        title,
                        hint = stringResource(R.string.value),
                        maxLine = 1,
                    ) { title = it }
                    Gap(height = 16.dp)

                    MyTextField(
                        stringResource(R.string.description),
                        description,
                        hint = stringResource(R.string.description_hint),
                        maxLine = 3,
                    ) { description = it }
                }
            }

            Gap(height = 16.dp)

            UrlTextField(value = postUrl, postUrlUiState = postUrlUiState) {
                postUrl = it
            }

            Gap(height = 16.dp)

            Text(
                text = stringResource(R.string.tags),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
            )

            Gap(height = 7.dp)

            Text(
                text = stringResource(R.string.tag_des),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Gap(height = 16.dp)

            Column {
                tags.forEach { tagModel ->

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .padding(bottom = 10.dp)
                                .clickable {
                                    tags.remove(tagModel)
                                }.fillMaxWidth()
                                .height(44.dp)
                                .background(
                                    MaterialTheme.colorScheme.onPrimary,
                                    shape = RoundedCornerShape(10.dp),
                                ).padding(horizontal = MarginHorizontalSize),
                    ) {
                        Text(
                            text = tagModel.tag ?: "",
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                        )

                        Image(
                            painter = painterResource(id = R.drawable.error),
                            contentDescription = null,
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .clickable {
                                // todo add tag
                                isShowSearch = true
                            }.fillMaxWidth()
                            .height(44.dp)
                            .background(
                                MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(10.dp),
                            ).padding(horizontal = MarginHorizontalSize),
                ) {
                    Text(
                        text = stringResource(R.string.add_tag),
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                    )

                    Image(painter = painterResource(id = R.drawable.add), contentDescription = null)
                }
            }

            Gap(height = 16.dp)

            Text(
                text = stringResource(R.string.preview),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
            )

            Gap(height = 7.dp)

            Box(
                modifier =
                    Modifier.background(
                        MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(10.dp),
                    ),
            ) {
                PostItem(
                    autoPlay = false,
                    postModel =
                        PostModel.createPreviewModel(
                            title = title,
                            type = type,
                            link = link,
                            tags = tags,
                            url = mediaUri?.path ?: "",
                            content = description,
                            uri = mediaUri,
                        ),
                    isDetail = true,
                )
            }

            Gap(height = 16.dp)

            val buttonEnable =
                if (selectIndex == 1) {
                    title.isNotBlank() && description.isNotBlank() && postUrl.isNotBlank() && mediaUri != null
                } else {
                    title.isNotBlank() && description.isNotBlank() && postUrl.isNotBlank()
                }

            TextButton(
                enabled = buttonEnable,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.surfaceTint,
                        disabledContainerColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = MaterialTheme.colorScheme.tertiary.copy(0.3f),
                    ),
                shape = MaterialTheme.shapes.medium,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val postModel =
                            viewModel.submitLinkPost(
                                type,
                                PostCreateParam(
                                    title = title,
                                    content = description.convertStringToQuillJson(),
                                    type = type.getType(),
                                    url = postUrl,
                                    tags = tags.map { it.tag ?: "" },
                                    link = if (type == PostType.LINK) link else null,
                                ),
                            )

                        postModel?.let {
                            withContext(Dispatchers.Main) {
                                PostDetailActivity.start(context, url = it.url ?: "")
                            }
                        }
                    }
                },
            ) {
                Text(
                    text = stringResource(id = R.string.submit_post),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Gap(height = 48.dp)
        }
    }
}

@Composable
fun MyTextField(
    title: String = "",
    value: String,
    hint: String = "",
    maxLine: Int = 1,
    isError: Boolean = false,
    errorMessage: String = "",
    onValueChange: (String) -> Unit,
) {
    TextField(
        maxLines = maxLine,
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
        label =
            if (title.isBlank()) {
                null
            } else {
                {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                errorContainerColor = MaterialTheme.colorScheme.onPrimary,
                disabledIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                errorIndicatorColor = MaterialTheme.colorScheme.onPrimary,
            ),
        isError = value.isNotBlank() && isError,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = hint,
                style =
                    TextStyle(
                        color = MaterialTheme.colorScheme.tertiary.copy(0.3f),
                        fontWeight = FontWeight.Bold,
                    ),
            )
        },
        supportingText =
            if (!isError || value.isBlank()) {
                null
            } else {
                { Text(text = errorMessage) }
            },
    )
}

@Composable
fun UrlTextField(
    value: String,
    hint: String = "post-url",
    postUrlUiState: PostUrlUiState,
    onValueChange: (String) -> Unit,
) {
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.onPrimary,
                        RoundedCornerShape(10.dp),
                    ).padding(horizontal = MarginHorizontalSize, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = Urls.POST_URL_PREFIX,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                    ),
            )

            BasicTextField(
                modifier = Modifier.weight(1f),
                maxLines = 1,
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box {
                            innerTextField()
                            if (value.isEmpty()) {
                                Text(
                                    text = hint,
                                    style =
                                        TextStyle(
                                            color = MaterialTheme.colorScheme.tertiary.copy(0.3f),
                                            fontWeight = FontWeight.Bold,
                                        ),
                                )
                            }
                        }
                    }
                },
            )

            when (postUrlUiState) {
                PostUrlUiState.Empty -> {
                }

                PostUrlUiState.Error -> {
                    Image(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(id = R.drawable.error),
                        contentDescription = null,
                    )
                }

                PostUrlUiState.Loading -> {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                PostUrlUiState.Success -> {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF34C759),
                    )
                }
            }
        }

        if (postUrlUiState is PostUrlUiState.Error) {
            Gap(height = 10.dp)
            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MarginHorizontalSize),
                text = "URL is not available. Please choose a better one for your lovely meme",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
fun ResolutionProgressView(resolutionName: List<ResolutionProgress>) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        resolutionName.forEach {
            Text(
                text = it.resolution.getDisplayName(),
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.secondary,
                    ),
            )
            Gap(height = 7.dp)

            LinearProgressIndicator(
                progress = { it.progress / 100 },
                strokeCap = StrokeCap.Square,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                gapSize = 0.dp,
                color = MaterialTheme.colorScheme.surfaceTint,
                trackColor = MaterialTheme.colorScheme.secondary.copy(0.16f),
                drawStopIndicator = {},
            )

            if (resolutionName.last() != it) {
                Gap(height = 20.dp)
            } else {
                Gap(height = 5.dp)
            }
        }
    }
}

@Preview
@Composable
private fun PreviewProgressBar() {
    AppTheme(darkTheme = false) {
        Box(modifier = Modifier.background(color = Color.White)) {
            ResolutionProgressView(
                listOf(
                    ResolutionProgress(Resolution.RES_480P, 50f),
                    ResolutionProgress(Resolution.RES_720P, 30f),
                ),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewLoginPage() {
    AppTheme {
        SubmitScreen(
            viewModel = SubmitViewModel(),
            postUrlUiState = PostUrlUiState.Empty,
            uploadingMedia = false,
            linkModel = null,
            resolutionProgressList = listOf(),
            encodingMedia = true,
            submitting = true,
        )
    }
}

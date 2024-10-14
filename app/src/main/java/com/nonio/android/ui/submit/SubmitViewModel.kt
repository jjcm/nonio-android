package com.nonio.android.ui.submit

import WebSocketHelper
import android.content.Context
import android.net.Uri
import android.util.Patterns
import android.util.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonio.android.common.getImageSize
import com.nonio.android.common.readAsRequestBody
import com.nonio.android.model.LinkModel
import com.nonio.android.model.PostCreateParam
import com.nonio.android.model.PostModel
import com.nonio.android.model.PostType
import com.nonio.android.model.PostType.*
import com.nonio.android.model.Resolution
import com.nonio.android.model.getDisplayName
import com.nonio.android.network.NetworkHelper.apiService
import com.nonio.android.network.Urls
import com.nonio.android.network.toRequestBody
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber

class SubmitViewModel : ViewModel() {
    private val _linkModel = MutableStateFlow<LinkModel?>(null)
    val linkModel = _linkModel.asStateFlow()

    private val _postUrlUiState = MutableStateFlow<PostUrlUiState>(PostUrlUiState.Empty)
    val postUrlUiState = _postUrlUiState.asStateFlow()

    private val _uploadingMedia = MutableStateFlow<Boolean>(false)
    val uploadingMedia = _uploadingMedia.asStateFlow()

    private val _encodingMedia = MutableStateFlow<Boolean>(false)
    val encodingMedia = _encodingMedia.asStateFlow()

    private val _submitting = MutableStateFlow<Boolean>(false)
    val submitting = _submitting.asStateFlow()

    private val _resolutions = MutableStateFlow<List<ResolutionProgress>>(listOf())
    val resolutions: StateFlow<List<ResolutionProgress>> = _resolutions.asStateFlow()

    private var currentTempMediaName = ""
    private var mediaSize: Size? = null

    private var webSocketHelper: WebSocketHelper? = null

    private var currentUploadJob: Job? = null

    fun parseUrl(url: String) {
        viewModelScope.launch {
            if (!isValidUrl(url = url)) return@launch
            val requestBody = mapOf("url" to url).toRequestBody()
            runCatching {
                _linkModel.emit(apiService.parseExternalUrl(requestBody))
                Timber.d("LinkModel: $linkModel")
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun isValidUrl(url: String): Boolean = Patterns.WEB_URL.matcher(url).matches()

    fun checkPostUrl(postUrl: String) {
        viewModelScope.launch {
            if (postUrl.isBlank()) {
                _postUrlUiState.emit(PostUrlUiState.Empty)
                return@launch
            }

            runCatching {
                _postUrlUiState.emit(PostUrlUiState.Loading)
                val postUrlAvailable = apiService.postUrlAvailable(postUrl)
                if (postUrlAvailable == true) {
                    _postUrlUiState.emit(PostUrlUiState.Success)
                } else {
                    _postUrlUiState.emit(PostUrlUiState.Error)
                }
            }.onFailure {
                it.printStackTrace()
                _postUrlUiState.emit(PostUrlUiState.Error)
            }
        }
    }

    suspend fun submitLinkPost(
        postType: PostType,
        param: PostCreateParam,
    ): PostModel? {
        _submitting.emit(true)
        var realParam = param
        runCatching {
            if (postType == IMAGE || postType == VIDEO) {
                moveMedia(postType, oldUrl = currentTempMediaName, url = param.url)
                realParam = param.copy(width = mediaSize?.width, height = mediaSize?.height)
            }
            _submitting.emit(false)
            return apiService.postCreate(realParam)
        }.onFailure {
            it.printStackTrace()
        }
        _submitting.emit(false)
        return null
    }

    private suspend fun moveMedia(
        type: PostType,
        oldUrl: String,
        url: String,
    ) {
        if (type == IMAGE) {
            apiService.moveImage(oldUrl, url)
        } else {
            apiService.moveVideo(oldUrl.replace(".mp4", ""), url)
        }
    }

    fun uploadMedia(
        context: Context,
        uri: Uri,
        type: PostType,
        onOk: (Uri?) -> Unit,
    ) {
        currentUploadJob?.cancel()
        currentUploadJob =
            viewModelScope.launch {
                resetUploadState()
                currentTempMediaName = ""
                mediaSize = null
                webSocketHelper?.close()
                _resolutions.value = listOf()

                _uploadingMedia.emit(true)
                val requestBody = context.contentResolver.readAsRequestBody(uri)

                val filePart =
                    MultipartBody.Part.createFormData("files", uri.lastPathSegment, requestBody)

                runCatching {
                    if (type == VIDEO) {
                        val fileName = apiService.uploadVideo(filePart)

                        if (!fileName.isNullOrBlank()) {
                            webSocketHelper = WebSocketHelper(Urls.videoEncodeURL(fileName))
                            webSocketHelper?.connect()

                            webSocketHelper?.waitForCompletion { message ->
                                Timber.d("Video encoding progress $message")
                                val resolution = "resolution:"
                                if (message.contains(resolution)) {
                                    val size =
                                        StringBuffer(message)
                                            .replaceFirst(Regex(resolution), "")
                                            .split("x")
                                    mediaSize = Size(size[0].toInt(), size[1].toInt())
                                    Timber.d("Video width and height $mediaSize")
                                    val resolutionList =
                                        Resolution.getLowerResolutions(
                                            mediaSize!!.width,
                                            mediaSize!!.height,
                                        )
                                    print("Video encoding list $resolutionList")
                                    _resolutions.value =
                                        resolutionList.map { ResolutionProgress(it, 0f) }
                                    _uploadingMedia.value = false
                                    _encodingMedia.value = true
                                    print("Video encoding")
                                } else {
                                    runCatching {
                                        val data = message.split(":")
                                        if (message.contains("source:")) {
                                            updateProgress(
                                                _resolutions.value.last().resolution,
                                                data[1].toFloat(),
                                            )
                                        } else {
                                            val res =
                                                Resolution.entries.first { it.getDisplayName() == data[0] }
                                            updateProgress(res, data[1].toFloat())
                                        }
                                    }.onFailure {
                                        it.printStackTrace()
                                    }
                                }
                            }

                            webSocketHelper?.close()
                            print("Video encoding end")
                            _encodingMedia.value = false
                            val url = Urls.videoURL(fileName, isAddSuffix = false)
                            Timber.d("Video upload success, temporary address $url")

                            currentTempMediaName = fileName
                            onOk.invoke(Uri.parse(url))
                            return@launch
                        }
                    } else {
                        val fileName = apiService.uploadImage(filePart)
                        if (!fileName.isNullOrBlank()) {
                            val imageURL = Urls.imageURL(fileName)
                            Timber.d("Image upload success, temporary address $imageURL")
                            _uploadingMedia.emit(false)
                            currentTempMediaName = fileName
                            mediaSize = uri.getImageSize(context)
                            onOk.invoke(Uri.parse(imageURL))
                            return@launch
                        }
                    }
                }.onFailure {
                    it.printStackTrace()
                }
                _uploadingMedia.emit(false)
                _encodingMedia.emit(false)
                onOk.invoke(null)
                return@launch
            }
    }

    private fun updateProgress(
        resolution: Resolution,
        newProgress: Float,
    ) {
        _resolutions.value =
            _resolutions.value.map {
                if (it.resolution == resolution) {
                    it.copy(progress = newProgress)
                } else {
                    it
                }
            }
    }

    fun uploadMediaJob() {
    }

    fun setCurrentJob(job: Job) {
        currentUploadJob = job
    }

    private fun resetUploadState() {
        _uploadingMedia.value = false
        _encodingMedia.value = false
        _submitting.value = false
    }
}

sealed interface PostUrlUiState {
    data object Empty : PostUrlUiState

    data object Loading : PostUrlUiState

    data object Success : PostUrlUiState

    data object Error : PostUrlUiState
}

data class ResolutionProgress(
    val resolution: Resolution,
    val progress: Float,
)

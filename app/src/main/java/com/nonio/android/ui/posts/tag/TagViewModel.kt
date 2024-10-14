package com.nonio.android.ui.posts.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonio.android.model.TagModel
import com.nonio.android.model.TagsModel
import com.nonio.android.network.NetworkHelper.apiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class TagViewModel : ViewModel() {
    companion object {
        private val _currentTag = MutableStateFlow<TagModel?>(null)
        val currentTag = _currentTag.asStateFlow()

        suspend fun updateTag(tag: TagModel?) {
            _currentTag.emit(tag)
        }
    }

    private val _tags = MutableStateFlow<TagsModel?>(null)
    val tags = _tags.asStateFlow()

    init {
        viewModelScope.launch {
            getTags()
        }
    }

    private suspend fun getTags() {
        runCatching {
            val tagsModel = apiService.tags()
            _tags.emit(tagsModel)
        }.onFailure {
            Timber.e(it, "load tag error")
        }
    }
}

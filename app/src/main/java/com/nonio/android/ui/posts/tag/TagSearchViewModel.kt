package com.nonio.android.ui.posts.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonio.android.model.TagsModel
import com.nonio.android.network.NetworkHelper.apiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TagSearchViewModel : ViewModel() {
    private val _tags = MutableStateFlow<TagsModel?>(null)
    val tags = _tags.asStateFlow()

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            runCatching {
                apiService.tagQuery(query)
            }.onSuccess {
                _tags.emit(it)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            _tags.emit(null)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}

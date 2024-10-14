package com.nonio.android.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nonio.android.model.UserInfoModel
import com.nonio.android.network.NetworkHelper.apiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AccountViewModel(
    private val userName: String,
) : ViewModel() {
    private val _userInfo = MutableStateFlow<UserInfoModel?>(null)
    val userInfo = _userInfo.asStateFlow()

    init {
        viewModelScope.launch {
            userInfo(true)
        }
    }

    private suspend fun userInfo(showLoading: Boolean = true) {
        if (showLoading) {
            // _loginUiState.emit(LoginUiState.Loading)
        }
        runCatching {
            apiService.userInfo(userName)
        }.onSuccess {
            Timber.d("Successfully retrieved user information: $it")
            _userInfo.emit(it)
        }.onFailure {
            Timber.d(it, "Failed to retrieve user information")
        }
    }
}

class AccountViewModelFactory(
    private val userName: String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            return AccountViewModel(userName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

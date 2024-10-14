package com.nonio.android.ui.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonio.android.BuildConfig
import com.nonio.android.common.UserHelper
import com.nonio.android.common.showToast
import com.nonio.android.model.UserInfoModel
import com.nonio.android.network.NetworkHelper.apiService
import com.nonio.android.network.toRequestBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class LoginViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    init {
        viewModelScope.launch {
            userInfo(false)
        }
    }

    val email =
        savedStateHandle.getStateFlow(
            "email",
            initialValue = "",
        )
    val pwd =
        savedStateHandle.getStateFlow(
            "pwd",
            initialValue = "",
        )

    val userInfo =
        savedStateHandle.getStateFlow<UserInfoModel?>(
            "userInfo",
            initialValue = null,
        )

    fun onEmailChange(email: String) {
        savedStateHandle["email"] = email
    }

    fun onPwdChange(pwd: String) {
        savedStateHandle["pwd"] = pwd
    }

    private val _loginUiState = MutableStateFlow<LoginUiState>(if (UserHelper.isLogin()) LoginUiState.HasLogin else LoginUiState.NoLogin)

    val loginUiState: StateFlow<LoginUiState> =
        _loginUiState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = LoginUiState.NoLogin,
            )

    private suspend fun userInfo(showLoading: Boolean = true) {
        if (showLoading) {
            _loginUiState.emit(LoginUiState.Loading)
        }
        runCatching {
            apiService.userInfo(UserHelper.getUserModel()?.username ?: "")
        }.onSuccess {
            Timber.d("Successfully retrieved user information: $it")
            savedStateHandle["userInfo"] = it!!
            _loginUiState.emit(LoginUiState.HasLogin)
        }.onFailure {
            Timber.d(it, "Failed to retrieve user information")
            _loginUiState.emit(LoginUiState.Error)
        }
    }

    fun login() {
        viewModelScope.launch {
            _loginUiState.emit(LoginUiState.Loading)
            runCatching {
                apiService.login(
                    mapOf(
                        "email" to email.value,
                        "password" to pwd.value,
                    ).toRequestBody(),
                )
            }.onSuccess {
                Timber.d("Login successful: $it")
                it?.let {
                    UserHelper.login(it)
                    userInfo()
                }
            }.onFailure {
                Timber.d(it, "Login failed")
                if (it is HttpException) {
                    Timber.e(it.response()?.errorBody()?.string())
                    if (it.response()?.code() == 404) {
                        _loginUiState.emit(LoginUiState.UserInvalid)
                        return@onFailure
                    }
                } else {
                    "Unknown error".showToast()
                }
                _loginUiState.emit(LoginUiState.Error)
            }
        }
    }
}

sealed interface LoginUiState {
    data object Error : LoginUiState

    data object Loading : LoginUiState

    data object HasLogin : LoginUiState

    data object NoLogin : LoginUiState

    data object UserInvalid : LoginUiState
}

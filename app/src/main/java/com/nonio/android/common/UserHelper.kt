package com.nonio.android.common

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.nonio.android.model.UserModel
import com.nonio.android.model.UserModelJsonAdapter
import com.nonio.android.network.Urls
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

interface UserProvider {
    fun init(context: Context)

    fun getToken(): String

    fun isLogin(): Boolean

    fun getRefreshToken(): String

    fun updateToken(
        refreshToken: String,
        accessToken: String,
    )
}

object UserHelper : UserProvider {
    private lateinit var context: WeakReference<Context>

    private const val KEY_USER_MODEL = "user_model"

    private var userModel: UserModel? = null

    private val jsonAdapter = UserModelJsonAdapter(Moshi.Builder().build())

    override fun isLogin(): Boolean = getUserModel() != null

    override fun getRefreshToken(): String {
        getUserModel()?.let {
            return it.refreshToken ?: ""
        }
        return ""
    }

    fun getUserModel(): UserModel? {
        if (userModel == null) {
            val userModelJson = SPUtils.getInstance(context.get()).getString(KEY_USER_MODEL, "")
            if (userModelJson.isNotBlank()) {
                val model = getUserModelByJson(userModelJson)
                userModel = model
            }
        }
        return userModel
    }

    fun login(userModel: UserModel) {
        SPUtils.getInstance(context.get()).put(KEY_USER_MODEL, jsonAdapter.toJson(userModel))
        this.userModel = userModel
        appViewModel().isLogin = true
        appViewModel().viewModelScope.launch {
            appViewModel().refreshVote()
        }
    }

    fun logout() {
        SPUtils.getInstance(context.get()).remove(KEY_USER_MODEL)
        userModel = null
        appViewModel().isLogin = false
        appViewModel().viewModelScope.launch {
            appViewModel().refreshVote()
        }
    }

    private fun getUserModelByJson(userModelJson: String): UserModel? = jsonAdapter.fromJson(userModelJson)

    fun getUserAvatar(): String {
        getUserModel()?.let {
            return Urls.avatarImageURL(it.username ?: "")
        }
        return ""
    }

    fun getUserName(): String {
        getUserModel()?.let {
            return it.username ?: ""
        }
        return ""
    }

    override fun init(context: Context) {
        this.context = WeakReference(context)
    }

    override fun getToken(): String {
        getUserModel()?.let {
            return it.accessToken ?: ""
        }
        return ""
    }

    override fun updateToken(
        refreshToken: String,
        accessToken: String,
    ) {
        getUserModel()?.copy(accessToken = accessToken, refreshToken = refreshToken)?.let {
            SPUtils.getInstance(context.get()).put(KEY_USER_MODEL, jsonAdapter.toJson(it))
            this.userModel = it
        }
    }
}

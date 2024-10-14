package com.nonio.android.ui.inbox

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonio.android.common.appViewModel
import com.nonio.android.model.Notification
import com.nonio.android.network.NetworkHelper
import com.nonio.android.network.toRequestBody
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.min

class InboxViewModel : ViewModel() {
    var refreshState by mutableStateOf(false)

    val notifications = mutableStateListOf<Notification>()

    private var job: Job? = null

    init {
    }

    private fun upCountData(list: List<Notification>?) {
        var count = 0
        list?.let {
            it.forEach { nt ->
                if (nt.read == false) {
                    count++
                }
            }
        }
        appViewModel().notificationCount = count
    }

    fun getNotifications(autoRefresh: Boolean = true) {
        var refresh = autoRefresh
        job?.cancel()

        job =
            viewModelScope.launch {
                var exponentialBackoff = 1L
                val maxDelay = 30 * 60 * 1000L

                while (isActive) {
                    runCatching {
                        if (refresh) {
                            refreshState = true
                        }

                        val data = NetworkHelper.apiService.notifications("false")
                        notifications.clear()
                        val list = data?.notifications?.asReversed() ?: emptyList()
                        notifications.addAll(list)
                        // tempNotifications.emit(data?.notifications?.asReversed() ?: emptyList())
                        if (refresh) {
                            refreshState = false
                        }

                        upCountData(data?.notifications)
                    }.onFailure {
                        if (refresh) {
                            refreshState = false
                        }

                        delay(exponentialBackoff)
                        exponentialBackoff = min((exponentialBackoff * 1.3).toLong(), maxDelay) // 退避
                        it.printStackTrace()
                    }
                    refresh = false
                    delay(10 * 1000L)
                }
            }
    }

    fun stopRequest() {
        job?.cancel()
    }

    fun markRead(id: Int) {
        viewModelScope.launch {
            NetworkHelper.apiService.markRead(
                mapOf("id" to id).toRequestBody(),
            )
        }
        if (appViewModel().notificationCount != 0) {
            appViewModel().notificationCount = appViewModel().notificationCount--
        }
    }

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun clearNotification() {
        notifications.clear()
    }
}

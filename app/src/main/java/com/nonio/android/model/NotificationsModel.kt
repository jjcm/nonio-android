package com.nonio.android.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationsModel(
    val notifications: List<Notification>?,
)

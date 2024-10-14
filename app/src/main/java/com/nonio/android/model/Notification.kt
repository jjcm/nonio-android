package com.nonio.android.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Notification(
    val comment_id: Int?,
    val content: String?,
    val date: Long?,
    val downvotes: Int?,
    val edited: Boolean?,
    val id: Int?,
    val parent: Int?,
    val parent_content: String?,
    val post: String?,
    val post_title: String?,
    val post_type: String?,
    var read: Boolean?,
    val upvotes: Int?,
    var user: String?,
)

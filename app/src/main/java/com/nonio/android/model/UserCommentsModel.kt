package com.nonio.android.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserCommentsModel(
    @Json(name = "comments")
    val comments: List<UserCommentModel>?,
)

@JsonClass(generateAdapter = true)
data class UserCommentModel(
    @Json(name = "childCount")
    val childCount: Int?,
    @Json(name = "content")
    val content: String?,
    @Json(name = "downvotes")
    val downvotes: Int?,
    @Json(name = "id")
    val id: String?,
    @Json(name = "parent")
    val parent: Any?,
    @Json(name = "post")
    val post: String?,
    @Json(name = "threadDownvotes")
    val threadDownvotes: Int?,
    @Json(name = "threadUpvotes")
    val threadUpvotes: Int?,
    @Json(name = "time")
    val time: Long?,
    @Json(name = "upvotes")
    val upvotes: Int?,
    @Json(name = "user")
    val user: String?,
)

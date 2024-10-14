package com.nonio.android.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentsModel(
    @Json(name = "comments")
    val comments: List<CommentModel>?,
)

package com.nonio.android.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PostsModel(
    @Json(name = "posts")
    val postModels: List<PostModel>?,
)

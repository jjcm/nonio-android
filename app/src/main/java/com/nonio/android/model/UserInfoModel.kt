package com.nonio.android.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class UserInfoModel(
    @Json(name = "description")
    val description: String? = "",
    @Json(name = "posts")
    val posts: Int? = null,
    @Json(name = "comments")
    val comments: Int? = null,
    @Json(name = "karma")
    val postKarma: Int? = null,
    @Json(name = "comment_karma")
    val commentKarma: Int? = null,
) : Serializable

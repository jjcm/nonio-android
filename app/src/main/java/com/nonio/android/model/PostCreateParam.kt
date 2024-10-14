package com.nonio.android.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PostCreateParam(
    val content: String,
    val title: String,
    val type: String,
    val url: String,
    val tags: List<String>,
    val link: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)

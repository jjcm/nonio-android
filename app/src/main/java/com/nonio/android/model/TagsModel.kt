package com.nonio.android.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TagsModel(
    @Json(name = "tags")
    val tags: List<TagModel>?,
)

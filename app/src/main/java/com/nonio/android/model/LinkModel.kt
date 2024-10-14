package com.nonio.android.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LinkModel(
    @Json(name = "description")
    val description: String?,
    @Json(name = "image")
    val image: String?,
    @Json(name = "title")
    val title: String?,
) {
    override fun toString(): String = "LinkModel(description=$description, image=$image, title=$title)"
}

package com.nonio.android.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.nonio.android.common.appViewModel
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TagModel(
    @Json(name = "postID")
    val postID: String?,
    @Json(name = "score")
    val score: Int?,
    @Json(name = "tag")
    val tag: String?,
    @Json(name = "tagID")
    val tagID: String?,
    @Json(name = "count")
    var count: Int?,
    @Json(ignore = true)
    val localScore: MutableState<Int> = mutableIntStateOf(score ?: 0),
    @Json(ignore = true)
    val isLiked: MutableState<Boolean> = mutableStateOf(appViewModel().isLiked(postID, tagID)),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TagModel

        return tag == other.tag
    }

    override fun hashCode(): Int = tag?.hashCode() ?: 0
}

data class LikeEvent(
    val isLiked: Boolean,
    val postID: String?,
    val tag: String?,
)

data class TagEvent(
    val postId: String?,
    val tagId: String?,
    val tag: String,
)

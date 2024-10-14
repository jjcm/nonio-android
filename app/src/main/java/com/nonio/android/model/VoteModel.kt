package com.nonio.android.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VoteModel(
    @Json(name = "postID")
    val postID: String?,
    @Json(name = "tagID")
    val tagID: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VoteModel

        if (postID != other.postID) return false
        if (tagID != other.tagID) return false

        return true
    }

    override fun hashCode(): Int {
        var result = postID?.hashCode() ?: 0
        result = 31 * result + (tagID?.hashCode() ?: 0)
        return result
    }
}

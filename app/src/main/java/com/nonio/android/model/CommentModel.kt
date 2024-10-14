package com.nonio.android.model

import com.nonio.android.common.formatDuration
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentModel(
    @Json(name = "content")
    val content: String?,
    @Json(name = "date")
    val date: Long?,
    @Json(name = "descendent_comment_count")
    val descendentCommentCount: Int?,
    @Json(name = "downvotes")
    val downvotes: Int?,
    @Json(name = "edited")
    val edited: Boolean?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "lineage_score")
    val lineageScore: Int?,
    @Json(name = "parent")
    val parent: Int?,
    @Json(name = "post")
    val post: String?,
    @Json(name = "post_title")
    val postTitle: String?,
    @Json(name = "upvotes")
    val upvotes: Int?,
    @Json(name = "user")
    val user: String?,
    @Json(ignore = true)
    val childList: MutableList<CommentModel> = mutableListOf(),
    @Json(ignore = true)
    var level: Int = 1,
) {
    fun getFormatTime(): String {
        if (date != null) {
            return formatDuration(date)
        }
        return ""
    }

    fun isTopComment(): Boolean = parent == 0
}

package com.nonio.android.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentVotesModel(
    @Json(name = "commentVotes")
    val commentVotes: List<CommentVoteModel>?,
)

@JsonClass(generateAdapter = true)
data class CommentVoteModel(
    @Json(name = "comment_id")
    val commentId: Int?,
    @Json(name = "upvote")
    val upvote: Boolean?,
)

package com.nonio.android.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VotesModel(
    @Json(name = "votes")
    val votes: List<VoteModel>,
)

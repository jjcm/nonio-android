package com.nonio.android.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserModel(
    @Json(name = "accessToken")
    val accessToken: String?,
    @Json(name = "refreshToken")
    val refreshToken: String?,
    @Json(name = "roles")
    val roles: List<String>?,
    @Json(name = "username")
    val username: String?,
) {
    override fun toString(): String = "UserModel(accessToken=$accessToken, refreshToken=$refreshToken, roles=$roles, username=$username)"
}

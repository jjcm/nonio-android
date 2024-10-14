package com.nonio.android.model

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.nonio.android.common.formatDuration
import com.nonio.android.model.PostType.*
import com.nonio.android.network.Urls
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PostModel(
    @Json(name = "commentCount")
    val commentCount: Int?,
    @Json(name = "content")
    val content: String?,
    @Json(name = "height")
    val height: Int?,
    @Json(name = "ID")
    val id: String?,
    @Json(name = "link")
    val link: String?,
    @Json(name = "score")
    val score: Int?,
    @Json(name = "tags")
    val tags: List<TagModel>?,
    @Json(name = "time")
    val time: Long?,
    @Json(name = "title")
    val title: String?,
    @Json(name = "type")
    val type: String?,
    @Json(name = "url")
    val url: String?,
    @Json(name = "user")
    val user: String?,
    @Json(name = "width")
    val width: Int?,
    @Json(ignore = true)
    val isPreview: Boolean = false,
    @Json(ignore = true)
    val previewMedia: Uri? = null,
    @Json(ignore = true)
    val mutableTags: SnapshotStateList<TagModel> =
        mutableStateListOf<TagModel>().also {
            it.addAll(
                tags ?: emptyList(),
            )
        },
) {
    companion object {
        fun createPreviewModel(
            title: String,
            type: PostType,
            link: String,
            tags: List<TagModel>?,
            content: String,
            url: String,
            uri: Uri?,
        ): PostModel =
            PostModel(
                0,
                content = content,
                0,
                "",
                link,
                0,
                tags,
                System.currentTimeMillis(),
                title = title,
                type.getType(),
                url,
                "user",
                0,
                previewMedia = uri,
                isPreview = true,
            )
    }

    fun getPostType(): PostType =
        when (type) {
            "image" -> IMAGE
            "video" -> VIDEO
            "text" -> TEXT
            "html" -> HTML
            "blog" -> BLOG
            else -> LINK
        }

    fun getFormatTime(): String {
        if (time != null) {
            return formatDuration(time)
        }
        return ""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PostModel

        if (commentCount != other.commentCount) return false
        if (content != other.content) return false
        if (height != other.height) return false
        if (id != other.id) return false
        if (link != other.link) return false
        if (score != other.score) return false
        if (tags != other.tags) return false
        if (time != other.time) return false
        if (title != other.title) return false
        if (type != other.type) return false
        if (url != other.url) return false
        if (user != other.user) return false
        if (width != other.width) return false

        return true
    }

    override fun hashCode(): Int {
        var result = commentCount ?: 0
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (height ?: 0)
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (link?.hashCode() ?: 0)
        result = 31 * result + (score ?: 0)
        result = 31 * result + (tags?.hashCode() ?: 0)
        result = 31 * result + (time?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (user?.hashCode() ?: 0)
        result = 31 * result + (width ?: 0)
        return result
    }

    fun getMediaUrl(): String =
        if (!isPreview) {
            when (getPostType()) {
                IMAGE -> Urls.imageURL(url ?: "")
                VIDEO -> Urls.videoURL(url ?: "")
                TEXT -> url ?: ""
                HTML -> url ?: ""
                LINK -> Urls.imageURL(url ?: "")
                BLOG -> url ?: ""
            }
        } else {
            url ?: ""
        }
}

enum class PostType {
    IMAGE,
    VIDEO,
    TEXT,
    HTML,
    LINK,
    BLOG,
}

fun PostType.getType(): String =
    when (this) {
        IMAGE -> "image"
        VIDEO -> "video"
        TEXT -> "text"
        HTML -> "html"
        LINK -> "link"
        BLOG -> "blog"
    }

data class PostSortModel(
    val sort: PostSortType,
    val time: PostTimeType? = null,
)

enum class PostSortType(
    val type: String,
) {
    POPULAR("Popular"),
    NEW("New"),
    TOP("Top"),
}

enum class PostTimeType(
    val type: String,
) {
    ALL("All"),
    YEAR("Year"),
    MONTH("Month"),
    WEEK("Week"),
    DAY("Day"),
}

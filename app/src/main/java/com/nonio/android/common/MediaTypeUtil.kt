package com.nonio.android.common

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap

object MediaTypeUtil {
    private fun getMimeType(
        context: Context,
        uri: Uri,
    ): String? =
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val contentResolver = context.contentResolver
            contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension?.toLowerCase())
        }

    fun getMediaType(
        context: Context,
        uri: Uri,
    ): MediaType {
        val mimeType = getMimeType(context, uri)
        return when {
            mimeType == null -> MediaType.UNKNOWN
            mimeType.startsWith("image/") -> MediaType.IMAGE
            mimeType.startsWith("video/") -> MediaType.VIDEO
            else -> MediaType.UNKNOWN
        }
    }

    enum class MediaType {
        IMAGE,
        VIDEO,
        UNKNOWN,
    }
}

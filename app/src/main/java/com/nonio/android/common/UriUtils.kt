package com.nonio.android.common

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Size
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

// Extension function to get image dimensions from a Uri
fun Uri.getImageSize(context: Context): Size? {
    context.contentResolver.openInputStream(this)?.use { inputStream ->
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(inputStream, null, options)
        return Size(options.outWidth, options.outHeight)
    }
    return null
}

// Extension function to get video dimensions from a Uri
fun Uri.getVideoSize(context: Context): Size? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, this)
        val width =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
        val height =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
        if (width != null && height != null) {
            Size(width, height)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    } finally {
        retriever.release()
    }
}

fun ContentResolver.readAsRequestBody(uri: Uri): RequestBody =
    object : RequestBody() {
        override fun contentType(): MediaType? = this@readAsRequestBody.getType(uri)?.toMediaTypeOrNull()

        override fun writeTo(sink: BufferedSink) {
            this@readAsRequestBody.openInputStream(uri)?.source()?.use(sink::writeAll)
        }

        override fun contentLength(): Long =
            this@readAsRequestBody.query(uri, null, null, null, null)?.use { cursor ->
                val sizeColumnIndex: Int = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                cursor.getLong(sizeColumnIndex)
            } ?: super.contentLength()
    }

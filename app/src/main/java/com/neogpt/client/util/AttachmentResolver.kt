package com.neogpt.client.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.neogpt.client.domain.model.Attachment

object AttachmentResolver {

    fun fromUri(context: Context, uri: Uri): Attachment {
        val resolver = context.contentResolver
        var name = uri.lastPathSegment?.substringAfterLast('/') ?: "Attachment"
        var size: Long? = null
        val mimeType = resolver.getType(uri) ?: "application/octet-stream"

        runCatching {
            resolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && !cursor.isNull(nameIndex)) {
                        name = cursor.getString(nameIndex) ?: name
                    }
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }
        }

        runCatching {
            resolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return Attachment(
            uri = uri.toString(),
            name = name,
            mimeType = mimeType,
            sizeBytes = size,
        )
    }
}

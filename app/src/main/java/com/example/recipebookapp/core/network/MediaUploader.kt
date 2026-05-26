package com.example.recipebookapp.core.network

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import com.example.recipebookapp.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaUploader @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val apiService: ApiService,
) {
    private val baseServerOrigin = BuildConfig.BASE_URL.trim().toUri()

    fun isLocalUri(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        return value.startsWith("content://") || value.startsWith("file://")
    }

    suspend fun uploadFromUri(uriString: String): String {
        val uri = uriString.toUri()
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Unable to read selected file")
        val fileName = queryDisplayName(context.contentResolver, uri) ?: "image.jpg"
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
        val uploaded = apiService.uploadMedia(part).url
        return if (uploaded.startsWith("http")) uploaded else BuildConfig.BASE_URL.trimEnd('/') + uploaded
    }

    suspend fun resolveForServerStorage(value: String): String {
        val resolved = if (isLocalUri(value)) uploadFromUri(value) else value.trim()
        return toServerStoredPath(resolved)
    }

    private fun toServerStoredPath(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return trimmed
        val uri = runCatching { trimmed.toUri() }.getOrNull() ?: return trimmed
        val isSameServer =
            uri.scheme.equals(baseServerOrigin.scheme, ignoreCase = true) &&
                uri.host.equals(baseServerOrigin.host, ignoreCase = true) &&
                effectivePort(uri) == effectivePort(baseServerOrigin)
        if (!isSameServer) return trimmed

        val path = uri.encodedPath ?: return trimmed
        val query = uri.encodedQuery?.let { "?$it" }.orEmpty()
        return "$path$query"
    }

    private fun effectivePort(uri: Uri): Int =
        if (uri.port != -1) uri.port else if (uri.scheme.equals("https", ignoreCase = true)) 443 else 80

    private fun queryDisplayName(contentResolver: ContentResolver, uri: Uri): String? {
        if (uri.scheme == "file") {
            return uri.lastPathSegment?.substringAfterLast('/')
        }
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
    }
}

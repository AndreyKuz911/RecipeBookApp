package com.example.recipebookapp.core.network

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.recipebookapp.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaUploader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
) {
    fun isLocalUri(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        return value.startsWith("content://") || value.startsWith("file://")
    }

    suspend fun uploadFromUri(uriString: String): String {
        val uri = Uri.parse(uriString)
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Unable to read selected file")
        val fileName = queryDisplayName(context.contentResolver, uri) ?: "image.jpg"
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
        val uploaded = apiService.uploadMedia(part).url
        return if (uploaded.startsWith("http")) uploaded else BuildConfig.BASE_URL.trimEnd('/') + uploaded
    }

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

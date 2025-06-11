package com.mobile.compose.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {
    fun uriToFile(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver
        val fileExtension = getFileExtension(context, uri) ?: "tmp"
        val fileName = "upload_${System.currentTimeMillis()}.$fileExtension"
        val tempFile = File(context.cacheDir, fileName)

        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        val type = context.contentResolver.getType(uri)
        return when {
            type != null -> MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
            uri.path != null -> {
                val lastDot = uri.path?.lastIndexOf('.')
                if (lastDot != -1 && lastDot != null && lastDot < uri.path!!.length - 1) {
                    uri.path?.substring(lastDot + 1)
                } else null
            }
            else -> null
        }
    }
}
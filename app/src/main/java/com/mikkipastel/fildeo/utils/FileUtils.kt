package com.mikkipastel.fildeo.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.mikkipastel.fildeo.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.lang.RuntimeException
import kotlin.math.min

class FileUtils {

    @SuppressLint("NewApi")
    fun getPath(context: Context, uri: Uri): String? {

        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val filename = getFileName(context, uri)

                val id = DocumentsContract.getDocumentId(uri)
                return if (id.toCharArray().all { it.isDigit() }) {
                    val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), id.toLong())
                    try {
                        getDataColumn(context, contentUri, null, null)
                    } catch (e: RuntimeException) {
                        setDownloadFilePath(filename!!)
                    }
                } else {
                    setDownloadFilePath(filename!!)
                }
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            } else if (isGoogleDriveUri(uri)) {
                return getDataColumnGoogleDrive(context, uri)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return when {
                isGooglePhotosUri(uri) -> uri.lastPathSegment
                isFromAppProvider(uri) -> uri.path?.replace("external_files", "storage/emulated/0")
                else -> getDataColumn(context, uri, null, null)
            }

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }

        return null
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun getDataColumnGoogleDrive(context: Context, uri: Uri?): String? {

        var cursor: Cursor? = null
        val column = "_display_name"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(column))
                val file = File(context.cacheDir, name)

                val inputStream = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable = inputStream.available()

                val bufferSize = min(bytesAvailable, maxBufferSize)

                val buffers = ByteArray(bufferSize)
                var bytesRead : Int
                while (inputStream.read(buffers).also { bytesRead = it } >= 0) {
                    outputStream.write(buffers, 0, bytesRead)
                }
                inputStream.close()
                outputStream.close()
                return file.path
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun setDownloadFilePath(filename: String): String {
        return Environment.getExternalStorageDirectory().toString() + "/Download/$filename"
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.contentprovider" == uri.authority
    }

    private fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority
    }

    private fun isFromAppProvider(uri: Uri): Boolean {
        return "${BuildConfig.APPLICATION_ID}.provider" == uri.authority
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}
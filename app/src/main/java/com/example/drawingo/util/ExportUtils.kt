package com.example.drawingo.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream

object ExportUtils {

    /**
     * Saves a video file to the device gallery using MediaStore.
     */
    fun saveVideoToGallery(context: Context, videoFile: File, fileName: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.mp4")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/Drawingo")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        
        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        
        uri?.let {
            var outputStream: OutputStream? = null
            var inputStream: FileInputStream? = null
            try {
                outputStream = contentResolver.openOutputStream(it)
                inputStream = FileInputStream(videoFile)
                if (outputStream != null) {
                    inputStream.copyTo(outputStream)
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    contentResolver.update(it, contentValues, null, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                contentResolver.delete(it, null, null)
                return null
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        }
        return uri
    }

    /**
     * Saves a bitmap to the device gallery using MediaStore.
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String, isTransparent: Boolean): Uri? {
        val format = if (isTransparent) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val mimeType = if (isTransparent) "image/png" else "image/jpeg"
        val extension = if (isTransparent) "png" else "jpg"
        
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.$extension")
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Drawingo")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        
        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        
        uri?.let {
            var outputStream: OutputStream? = null
            try {
                outputStream = contentResolver.openOutputStream(it)
                if (outputStream != null) {
                    bitmap.compress(format, 100, outputStream)
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    contentResolver.update(it, contentValues, null, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                contentResolver.delete(it, null, null)
                return null
            } finally {
                outputStream?.close()
            }
        }
        return uri
    }
}

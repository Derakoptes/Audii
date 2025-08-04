package com.acube.audii.model

import com.acube.audii.model.database.Audiobook
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File

data class AudiobookData(
    val title:String,
    val author:String,
    val narrator:String,
    val filePath:String,
    val duration: List<Long>,
    val imageUri:String?
)

fun getImageFromPath(filePath: String): Bitmap? {
    val file = File(filePath)
    if (!file.exists()) {
        return null
    }
    val byteArrayOutputStream = ByteArrayOutputStream()
    file.inputStream().use { inputStream ->
        inputStream.copyTo(byteArrayOutputStream)
    }
    val byteArray = byteArrayOutputStream.toByteArray()
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}





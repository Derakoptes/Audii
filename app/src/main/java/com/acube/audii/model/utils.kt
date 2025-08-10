package com.acube.audii.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.ByteArrayOutputStream
import java.io.File

data class AudiobookData(
    val title:String,
    val author:String,
    val narrator:String,
    val uriString:String,
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
/*
* Compare  Uris
* */
fun compareUris(
    uri1: Uri,
    uri2: Uri
):Boolean{
    if(uri1 == uri2)return true
    if(uri1.lastPathSegment.equals(uri2.lastPathSegment))return true
    return uri1.path==uri2.path
}

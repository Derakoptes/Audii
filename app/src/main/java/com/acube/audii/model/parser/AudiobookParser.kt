package com.acube.audii.model.parser

import android.content.Context
import android.net.Uri
import com.acube.audii.model.AudiobookData

interface AudiobookParser {
    fun parseAudiobook(uri: Uri,context: Context): AudiobookData?

    fun addUriImageToDatabase(imageUri: Uri, extension: String,context: Context):String
    fun addByteArrayImageToDatabase(byteArray: ByteArray,context: Context):String
    fun generateUniqueName():String
}

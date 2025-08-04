package com.acube.audii.model.parser

import android.content.Context
import android.net.Uri
import com.acube.audii.model.AudiobookData

interface AudiobookParser {
    fun parseAudiobook(uri: Uri,context: Context): AudiobookData?
    //whichName defines whether the name of the file or the containing folder should be used
    //true means it should be which
    fun addUriImageToDatabase(imageUri: Uri, extension: String,context: Context):String
    fun addByteArrayImageToDatabase(byteArray: ByteArray,context: Context):String
    fun generateUniqueName():String
}

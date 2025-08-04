package com.acube.audii.model.parser

import android.net.Uri
import com.acube.audii.model.AudiobookData
import com.acube.audii.model.database.Audiobook

interface MapAudiobook {
    fun mapFolderToAudiobook(uri: Uri): AudiobookData//Single folder for an audiobook
    fun mapFileToAudiobook(uri: Uri):AudiobookData
    fun mapFolderToAudiobooks(uri: Uri):List<AudiobookData>//Adding multiple Audiobooks from one folder
}
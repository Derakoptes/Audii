package com.acube.audii.repository.parser

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.acube.audii.model.AudiobookData
import com.acube.audii.model.database.Audiobook
import com.acube.audii.model.parser.AudiobookParser
import com.acube.audii.model.parser.MapAudiobook
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MapAudiobookImpl @Inject constructor(
    private val parser: AudiobookParser,
    @ApplicationContext private val context: Context
) : MapAudiobook {
    override fun mapFolderToAudiobook(uri: Uri): AudiobookData {
        parser.parseAudiobook(uri, context)?.let {
            return it
        }
        throw Exception("Error Adding Audiobook")
    }

    override fun mapFileToAudiobook(uri: Uri): AudiobookData {
        parser.parseAudiobook(uri, context)?.let {
            return it
        }
        throw Exception("Error Adding Audiobook")
    }

    override fun mapFolderToAudiobooks(uri: Uri): List<AudiobookData> {
        val document = DocumentFile.fromSingleUri(context, uri)
        if (document?.isDirectory == false) throw Exception("Not a folder")
        if ((document?.listFiles()?.size ?: 0) <= 0) throw Exception("No files in folder")

        val audiobookData = mutableListOf<AudiobookData>()
        document?.listFiles()?.forEach { it ->
            parser.parseAudiobook(it.uri, context)?.let {
                audiobookData.add(it)
            }
        }
        return audiobookData
    }
}
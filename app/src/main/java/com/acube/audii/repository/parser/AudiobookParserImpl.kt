package com.acube.audii.repository.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.acube.audii.model.AudiobookData
import com.acube.audii.model.parser.AudiobookParser
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudiobookParserImpl @Inject constructor(): AudiobookParser {
    override fun parseAudiobook(
        uri: Uri,
        context: Context
    ): AudiobookData? {
        val documentFile = DocumentFile.fromSingleUri(context, uri) ?: return null
        val title = if (documentFile.isDirectory) {
            documentFile.name ?: "Unknown"
        } else {
            documentFile.name?.substringBeforeLast('.') ?: "Unknown"
        }

        val audioFiles = if (documentFile.isDirectory) {
            documentFile.listFiles()
                .filter { it.type?.startsWith("audio/") == true }
                .sortedBy { it.name }
        } else {
            listOf(documentFile)
        }

        if (audioFiles.isEmpty()) {
            return null
        }

        val firstAudioFile = audioFiles.first()
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, firstAudioFile.uri)

        //save and get image path
        val possibleImage = if( firstAudioFile.isDirectory )documentFile.listFiles().find { it.type?.startsWith("image/") == true }?.uri else null
        val imagePath = when (documentFile.isDirectory && possibleImage != null) {
            true -> {
                addUriImageToDatabase(
                    possibleImage,
                    possibleImage.path?.substringAfterLast(".") ?: "jpg",
                    context,
                )

            }

            false -> {
                if (retriever.embeddedPicture != null) {
                    addByteArrayImageToDatabase(
                        retriever.embeddedPicture!!,
                        context,

                        )
                } else {
                    null
                }
            }
        }

        val author =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown"
        val narrator =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?: "Unknown"
        val durations = audioFiles.map {
            val tempRetriever = MediaMetadataRetriever()
            tempRetriever.setDataSource(context, it.uri)
            val durationString =
                tempRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            tempRetriever.release()
            durationString?.toLongOrNull() ?: 0L
        }


        retriever.release()

        return AudiobookData(
            title = title,
            author = author,
            narrator = narrator,
            filePath = uri.toString(),
            duration = durations,
            imageUri = imagePath,
        )
    }


    override fun addUriImageToDatabase(
        imageUri: Uri,
        extension: String,
        context: Context,
    ): String {
        val fileName = "${generateUniqueName()}.$extension"
        val outputFile = File(context.filesDir, fileName)
        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            outputFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return outputFile.absolutePath
    }

    override fun addByteArrayImageToDatabase(
        byteArray: ByteArray,
        context: Context,
    ): String {
        val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
        val uniqueName = generateUniqueName()
        val fileName = "$uniqueName.png"

        val outputFile = File(context.filesDir, fileName)
        outputFile.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        return outputFile.absolutePath
    }

    override fun generateUniqueName(): String {
        return Date().time.toString()
    }


}
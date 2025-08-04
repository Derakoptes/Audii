package com.acube.audii.model.filePicker

import android.net.Uri

interface FilePickerProvider {
    suspend fun getFileData(folder: Boolean = false): Uri?
    fun registerFilePicker()
}
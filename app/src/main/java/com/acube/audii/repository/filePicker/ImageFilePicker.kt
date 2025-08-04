package com.acube.audii.repository.filePicker

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CompletableDeferred

class ImageFilePicker(
    private val activity: ComponentActivity
) {
    private var deferred: CompletableDeferred<Uri?>? = null

    private val pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest> =
        activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            deferred?.complete(uri)
        }

    suspend fun pickImage(): Uri? {
        deferred = CompletableDeferred()
        pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        return deferred?.await()
    }
}
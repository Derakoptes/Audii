package com.acube.audii.repository.filePicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.acube.audii.model.filePicker.FilePickerProvider
import kotlinx.coroutines.CompletableDeferred

class AudiobookPicker(
    private val activity: Activity,
    private val filePickerLauncher: ActivityResultLauncher<Intent>,
    private val permissionLauncher: ActivityResultLauncher<String>
) : FilePickerProvider {

    private var deferredUri: CompletableDeferred<Uri?>? = null
    private var deferredPermissionResult: CompletableDeferred<Boolean>? = null

    override suspend fun getFileData(folder: Boolean): Uri? {
        if (!checkStoragePermission()) {
            val permissionGranted = requestPermissions()
            if (!permissionGranted) {
                return null
            }
        }

        val pickerIntent = if (folder) {
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        } else {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
            }
        }

        deferredUri = CompletableDeferred()
        filePickerLauncher.launch(pickerIntent)
        return deferredUri?.await()
    }

    fun onFilePickerResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            uri?.let {
                activity.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            deferredUri?.complete(uri)
        } else {
            deferredUri?.complete(null)
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        deferredPermissionResult?.complete(isGranted)
    }

    private suspend fun requestPermissions(): Boolean {
        deferredPermissionResult = CompletableDeferred()
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        permissionLauncher.launch(permission)
        return deferredPermissionResult?.await() ?: false
    }

    private fun checkStoragePermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    override fun registerFilePicker() {
    }
}

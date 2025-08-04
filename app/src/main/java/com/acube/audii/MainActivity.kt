package com.acube.audii

import AudiobookListScreen
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope

import com.acube.audii.model.database.Audiobook
import com.acube.audii.repository.filePicker.AudiobookPicker
import com.acube.audii.ui.theme.AudiiTheme
import com.acube.audii.viewModel.AudiobookViewModel
import com.acube.audii.viewModel.ProcessorUiState
import com.acube.audii.viewModel.ProcessorViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val audiobookViewModel : AudiobookViewModel by viewModels()
    private val processorViewModel: ProcessorViewModel by viewModels()

    private lateinit var audiobookPicker: AudiobookPicker

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        audiobookPicker.onFilePickerResult(result.resultCode, result.data)
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        audiobookPicker.onPermissionResult(isGranted)
    }

    private suspend fun pickSingleFile() {
        withContext(Dispatchers.IO) {
            val uri = audiobookPicker.getFileData(folder = false)
            uri?.let {
                if (!audiobookViewModel.checkIfAudiobookExists(uri.toString())){
                    val audiobookData = processorViewModel.processSingleFile(it)
                    withContext(Dispatchers.IO) {
                        audiobookViewModel.addAudiobook(audiobookData)
                        withContext(Dispatchers.Main) {
                            showToast(
                                "Added audiobook: ${audiobookData.title}",
                                Toast.LENGTH_SHORT
                            )
                        }
                    }
                }else{
                    withContext(Dispatchers.Main) {
                        showToast(
                            "Audiobook already exists",
                            Toast.LENGTH_SHORT
                        )
                    }
                }
            }
        }
    }

    private suspend fun pickFolderForSingleAudiobook() {
        withContext(Dispatchers.IO) {
            val uri = audiobookPicker.getFileData(folder = true)
            uri?.let {
                if (!audiobookViewModel.checkIfAudiobookExists(uri.toString())) {
                    val audiobookData = processorViewModel.processFolderForSingleAudiobook(it)
                    withContext(Dispatchers.IO) {
                        audiobookViewModel.addAudiobook(audiobookData)
                        withContext(Dispatchers.Main) {
                            showToast(
                                "Added audiobook: ${audiobookData.title}",
                                Toast.LENGTH_SHORT
                            )
                        }
                    }
                }else{
                    withContext(Dispatchers.Main) {
                        showToast(
                            "Audiobook already exists",
                            Toast.LENGTH_SHORT
                        )
                    }
                }
            }
        }
    }

    private suspend fun pickFolderForMultipleAudiobooks() {
        withContext(Dispatchers.IO) {
            val uri = audiobookPicker.getFileData(folder = true)
            uri?.let {
                if (!audiobookViewModel.checkIfAudiobookExists(uri.toString())){
                    val audiobooksData = processorViewModel.processFolderForMultipleAudiobooks(it)
                    withContext(Dispatchers.IO) {
                        audiobooksData.forEach { data ->
                            audiobookViewModel.addAudiobook(data)
                        }
                        withContext(Dispatchers.Main) {
                            showToast(
                                "Added ${audiobooksData.size} audiobooks from folder",
                                Toast.LENGTH_SHORT
                            )
                        }
                    }
                }else{
                    withContext(Dispatchers.Main) {
                        showToast(
                            "Audiobook already exists",
                            Toast.LENGTH_SHORT
                        )
                    }
                }
            }
        }
    }
    private suspend fun showToast(message:String,length:Int){
        Toast.makeText(
            this@MainActivity,
            message,
            length
        ).show()
    }
    private fun handleAddAudiobook() {
        lifecycleScope.launch {
            pickSingleFile()
        }
    }

    fun addSingleFile() {
        lifecycleScope.launch {
            pickSingleFile()
        }
    }

    fun addFolderAsAudiobook() {
        lifecycleScope.launch {
            pickFolderForSingleAudiobook()
        }
    }

    fun addMultipleAudiobooksFromFolder() {
        lifecycleScope.launch {
            pickFolderForMultipleAudiobooks()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        audiobookPicker = AudiobookPicker(this, filePickerLauncher, permissionLauncher)

        val audiobooks: StateFlow<List<Audiobook>> = audiobookViewModel.audioBookUiState.value.audiobooks
        val loadingUiState: StateFlow<ProcessorUiState> = processorViewModel.uiState
        setContent {
            AudiiTheme {
                val processorUiState by processorViewModel.uiState.collectAsState()
                AudiobookListScreen(
                    audiobooks = audiobooks,
                    onAddAudiobook = { handleAddAudiobook() },
                    isAddingAudiobook = loadingUiState
                )
            }
        }
    }
}

@HiltAndroidApp
class MyApplication : Application()
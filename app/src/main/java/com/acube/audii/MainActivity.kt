package com.acube.audii

import AudiobookListScreen
import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.lifecycleScope

import com.acube.audii.model.database.Audiobook
import com.acube.audii.repository.filePicker.AudiobookPicker
import com.acube.audii.ui.theme.AudiiTheme
import com.acube.audii.view.mainScreen.player.PlayerSheet
import com.acube.audii.viewModel.AudiobookViewModel
import com.acube.audii.viewModel.PlayerUiState
import com.acube.audii.viewModel.PlayerViewModel
import com.acube.audii.viewModel.ProcessorUiState
import com.acube.audii.viewModel.ProcessorViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val audiobookViewModel : AudiobookViewModel by viewModels()
    private val processorViewModel: ProcessorViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()

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

    private fun formatTime(millis: Long): String {
        val absMillis = if (millis < 0) -millis else millis
        val hours = TimeUnit.MILLISECONDS.toHours(absMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(absMillis) -
                TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(absMillis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(absMillis))

        return if (hours > 0) {
            String.format(Locale.current.platformLocale, "%s%02d:%02d:%02d", if (millis < 0) "-" else "", hours, minutes, seconds)
        } else {
            String.format(Locale.current.platformLocale, "%s%02d:%02d", if (millis < 0) "-" else "", minutes, seconds)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        audiobookPicker = AudiobookPicker(this, filePickerLauncher, permissionLauncher)

        val audiobooks: StateFlow<List<Audiobook>> = audiobookViewModel.audioBookUiState.value.audiobooks
        val loadingUiState: StateFlow<ProcessorUiState> = processorViewModel.uiState
        val playerUiState: StateFlow<PlayerUiState> = playerViewModel.uiState

        setContent {
            AudiiTheme {
                val processorUiState by processorViewModel.uiState.collectAsState()
                val playerUiState by playerViewModel.uiState.collectAsState()
                var showPlayerSheet by remember { mutableStateOf(false) }
                
                if (showPlayerSheet && playerUiState.currentAudiobook != null) {
                    PlayerSheet(
                        playerState = playerUiState,
                        onPlayPause = { playerViewModel.playPause() },
                        onSkipNext = { playerViewModel.nextChapter()},
                        onSkipPrevious = { playerViewModel.previousChapter()},
                        onSkipForward = {  playerViewModel.skipForward()},
                        onSkipBackward = { playerViewModel.skipBackward() },
                        onSeekTo = { position -> playerViewModel.seekTo(position) },
                        onClose = { showPlayerSheet = false },
                        formatTime = { millis -> formatTime(millis) }
                    )
                } else {
                    AudiobookListScreen(
                        audiobooks = audiobooks,
                        onAudiobookClick = { audiobookId ->
                            lifecycleScope.launch {
                                val audiobook = audiobooks.value.find { it.id == audiobookId }
                                audiobook?.let { playerViewModel.playAudiobook(it) }
                            }
                        },
                        onAddAudiobook = { handleAddAudiobook() },
                        isAddingAudiobook = loadingUiState,
                        playerState = playerViewModel.uiState,
                        onPlayerPlayPause = {  },
                        onPlayerSkipNext = {  },
                        onPlayerSkipPrevious = {  },
                        onPlayerClick = { showPlayerSheet = true }
                    )
                }
            }
        }
    }
}

@HiltAndroidApp
class MyApplication : Application()
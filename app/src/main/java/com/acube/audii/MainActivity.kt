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
import androidx.compose.runtime.LaunchedEffect
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
import com.acube.audii.view.mainScreen.audiobookList.AddAudiobookDialog
import com.acube.audii.view.mainScreen.player.PlayerSheet
import com.acube.audii.viewModel.AudiobookViewModel
import com.acube.audii.viewModel.CollectionViewModel
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
    private val collectionViewModel: CollectionViewModel by viewModels()

    private lateinit var audiobookPicker: AudiobookPicker

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        audiobookPicker.onFilePickerResult(result.resultCode, result.data)
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        audiobookPicker.onPermissionResult(isGranted)
    }

    private suspend fun pickSingleFile() {
        withContext(Dispatchers.IO) {
            try {
                val uri = audiobookPicker.getFileData(folder = false)
                uri?.let {
                    val audiobookData = processorViewModel.processSingleFile(it)
                    withContext(Dispatchers.IO) {
                        audiobookViewModel.addAudiobook(audiobookData)
                        withContext(Dispatchers.Main) {
                            showToast(
                                "Added audiobook: ${audiobookData.title}",
                                Toast.LENGTH_LONG
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error adding single file: ${e.message}", Toast.LENGTH_LONG)
                }
            }

        }
    }

    private suspend fun pickFolderForSingleAudiobook() {
        withContext(Dispatchers.IO) {
            try {
                val uri = audiobookPicker.getFileData(folder = true)
                uri?.let {
                    val audiobookData = processorViewModel.processFolderForSingleAudiobook(it)
                    withContext(Dispatchers.IO) {
                        audiobookViewModel.addAudiobook(audiobookData)
                        withContext(Dispatchers.Main) {
                            showToast(
                                "Added audiobook: ${audiobookData.title}",
                                Toast.LENGTH_LONG
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast(
                        "Error adding folder as single audiobook: ${e.message}",
                        Toast.LENGTH_LONG
                    )
                }
            }

        }
    }

    private suspend fun pickFolderForMultipleAudiobooks() {
        withContext(Dispatchers.IO) {
            try {
                val uri = audiobookPicker.getFileData(folder = true)
                uri?.let {
                    val audiobooksData = processorViewModel.processFolderForMultipleAudiobooks(it)
                    if (audiobooksData.isNotEmpty()) {
                        audiobookViewModel.addDatasource(uri.toString())
                        withContext(Dispatchers.IO) {
                            audiobooksData.forEach { data ->
                                audiobookViewModel.addAudiobook(data)
                            }
                            withContext(Dispatchers.Main) {
                                showToast(
                                    "Added ${audiobooksData.size} audiobooks from folder",
                                    Toast.LENGTH_LONG
                                )
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            showToast("No audiobooks found in the selected folder.", Toast.LENGTH_LONG)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error adding multiple audiobooks from folder: ${e.message}", Toast.LENGTH_LONG)
                }
            }

        }
    }
    private fun showToast(message:String,length:Int){
        Toast.makeText(
            this@MainActivity,
            message,
            length
        ).show()
    }
    private fun handleAddAudiobook(type: ADD_TYPE) {
        lifecycleScope.launch {
            when(type){
                ADD_TYPE.ONE_FROM_FILE -> addSingleFile()
                ADD_TYPE.ONE_FROM_FOLDER -> addFolderAsAudiobook()
                ADD_TYPE.MULTIPLE_FROM_FOLDER -> addMultipleAudiobooksFromFolder()
            }
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
    private fun syncDatasources(){
        lifecycleScope.launch{
            try {
                val unAdded = audiobookViewModel.syncDatasources()
                if (unAdded.isNotEmpty()) {
                    showToast(
                        "Adding ${unAdded.size} new audiobooks",
                        Toast.LENGTH_SHORT
                    )
                    unAdded.forEach {
                        val audiobookData = processorViewModel.processSingleFile(it)
                        withContext(Dispatchers.IO) {
                            audiobookViewModel.addAudiobook(audiobookData)
                            withContext(Dispatchers.Main) {
                                showToast(
                                    "Added audiobook: ${audiobookData.title}",
                                    Toast.LENGTH_LONG
                                )
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                showToast(
                    "Error syncing datasources: ${e.message}",
                    Toast.LENGTH_LONG
                )
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        audiobookPicker = AudiobookPicker(this, filePickerLauncher, permissionLauncher)

        val audiobooks: StateFlow<List<Audiobook>> = audiobookViewModel.audioBookUiState.value.audiobooks
        val loadingUiState: StateFlow<ProcessorUiState> = processorViewModel.uiState

        fun stopAndSave(){
            audiobookViewModel.saveAudiobookProgress()
            playerViewModel.stopPlaying()
            audiobookViewModel.setCurrentAudiobook("")
        }
        setContent {
            AudiiTheme {
                var showAddDialog by remember { mutableStateOf(false) }
                LaunchedEffect(key1 = Unit) {
                    audiobookViewModel.setUpController()
                    syncDatasources()
                }
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
                        formatTime = { millis -> formatTime(millis) },
                        onChangeSpeed ={
                            speed->playerViewModel.changeSpeed(speed)
                                   audiobookViewModel.updateAudiobookSpeed(speed,playerUiState.currentAudiobook!!.id)
                                       },
                        onGoToChapter = {
                            playerViewModel.goToChapter(it)
                        }
                    )
                } else {
                    AudiobookListScreen(
                        audiobooks = audiobooks,
                        collectionState = collectionViewModel.collectionListUiState,
                        onAudiobookClick = { audiobookId ->
                            lifecycleScope.launch {
                                try {
                                    val audiobook = audiobooks.value.find { it.id == audiobookId }
                                    audiobook?.let {
                                        playerViewModel.playAudiobook(it)
                                        audiobookViewModel.setCurrentAudiobook(audiobook.id)
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        showToast(
                                            "Error playing audiobook: ${e.message}",
                                            Toast.LENGTH_LONG
                                        )
                                    }
                                }
                            }
                        },
                        onAddAudiobook = { showAddDialog = true },
                        isAddingAudiobook = loadingUiState,
                        playerState = playerViewModel.uiState,
                        audiobookUiState = audiobookViewModel.audioBookUiState,
                        onPlayerPlayPause = { playerViewModel.playPause() },
                        onPlayerSkipNext = { playerViewModel.nextChapter() },
                        onPlayerSkipPrevious = { playerViewModel.previousChapter() },
                        onPlayerClick = { showPlayerSheet = true },
                        onSwipeDown = {
                            stopAndSave()
                        },
                        clearAudiobookUiStateError = {
                            audiobookViewModel.clearAudiobookUiStateError()
                        },
                        clearProcessorUiStateError = {
                            processorViewModel.clearProcessorUiStateError()
                        },
                        clearCollectionErrorMessage = {collectionViewModel.clearErrorMessage()},
                        deleteCollection = {
                            collectionViewModel.deleteCollection(it)
                                           audiobookViewModel.removeCollectionFromAudiobooks(it.id)
                                           },
                        addCollection = {collectionViewModel.addCollection(it)},
                        addAudiobookToCollection = {collection,id->audiobookViewModel.updateAudiobookCollections(collection,id)}
                    )
                }
                if(showAddDialog)
                AddAudiobookDialog(
                    onDismiss = { showAddDialog=false },
                    onAddTypeSelected = { handleAddAudiobook(it) }
                )
            }
        }
    }

}



enum class ADD_TYPE {
    ONE_FROM_FILE,
    ONE_FROM_FOLDER,
    MULTIPLE_FROM_FOLDER
}
@HiltAndroidApp
class MyApplication : Application()
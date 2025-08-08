package com.acube.audii.viewModel

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acube.audii.model.AudiobookData
import com.acube.audii.model.database.Audiobook
import com.acube.audii.model.repository.AudiobookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import androidx.core.net.toUri
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.acube.audii.service.PlayerService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlin.text.get

data class AudiobookListUiState(
    val audiobooks: StateFlow<List<Audiobook>> = MutableStateFlow(emptyList()),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AudiobookViewModel @Inject constructor(
    private val repository: AudiobookRepository,
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    private val _audioBookUiState = MutableStateFlow(AudiobookListUiState())
    val audioBookUiState = _audioBookUiState.asStateFlow()

    private var currentPlayingAudiobook:String=""
    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>
    private val scope = CoroutineScope(Dispatchers.IO+ Job())
    private var mediaController: MediaController? = null

    init {
        loadAudioBooks()
    }

    private fun loadAudioBooks() {
        _audioBookUiState.value = _audioBookUiState.value.copy(isLoading = true)

        _audioBookUiState.value = _audioBookUiState.value.copy(
            audiobooks = repository.getAllAudiobooks().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            ),
            isLoading = false,
            errorMessage = null
        )
    }
     fun setCurrentAudiobook(id:String){
        currentPlayingAudiobook=id
    }
    fun setUpController() {
        val sessionToken = SessionToken(applicationContext, ComponentName(applicationContext, PlayerService::class.java))

        mediaControllerFuture = MediaController.Builder(applicationContext, sessionToken).buildAsync()

        mediaControllerFuture.addListener({
            mediaController = mediaControllerFuture.get()

            startTracking()
        }, MoreExecutors.directExecutor())
    }
    @OptIn(ExperimentalUuidApi::class)
    fun addAudiobook(audiobookData: AudiobookData) {
        viewModelScope.launch {
            try {
                repository.addAudiobook(
                    Audiobook(
                        id = Uuid.random().toHexString(),
                        title = audiobookData.title,
                        author = audiobookData.author,
                        uriString = audiobookData.uriString,
                        duration = audiobookData.duration,
                        currentPosition = Pair(0, 0L),
                        coverImageUriPath = audiobookData.imageUri ?: "",
                        modifiedDate = System.currentTimeMillis(),
                        narrator = audiobookData.narrator
                    )
                )
            } catch (e: Exception) {
                _audioBookUiState.value = _audioBookUiState.value.copy(
                    errorMessage = "Failed to add audiobook : ${e.message}"
                )
            }
        }
    }

    fun checkIfAudiobookExists(uriString: String): Boolean {
        var exists = false
        val possibleMatches= audioBookUiState.value.audiobooks.value.filter { it.uriString.substringAfterLast("/") == uriString.substringAfterLast("/") }

        possibleMatches.forEach { it->
            if (it.uriString.toUri().equals(uriString.toUri())){
                exists = true
            }
        }
        return exists
    }

    fun deleteAudiobook(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteAudiobook(id = id)
            } catch (e: Exception) {
                _audioBookUiState.value = _audioBookUiState.value.copy(
                    errorMessage = "Failed to add audiobook : ${e.message}"
                )
            }
        }
    }

    private fun clearErrorState() {
        _audioBookUiState.value = _audioBookUiState.value.copy(
            errorMessage = null
        )
    }
    fun saveAudiobookProgress(){
        viewModelScope.launch {
            try {
                repository.updatePlaybackPosition(currentPlayingAudiobook,position = Pair(
                    mediaController?.currentMediaItemIndex?:0,
                    mediaController?.currentPosition?:0L
                ))
            }catch (e: Exception){
                _audioBookUiState.value = _audioBookUiState.value.copy(
                    errorMessage = "Failed to add audiobook : ${e.message}"
                )
            }
        }
    }
    private fun startTracking(){
        scope.launch {
            while (true){
                if(currentPlayingAudiobook.isNotEmpty()){
                    saveAudiobookProgress()
                }
                delay(90*1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
        currentPlayingAudiobook=""
        mediaController?.release()
        mediaController=null
    }
}


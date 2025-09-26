package com.acube.audii.viewModel

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
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
import androidx.documentfile.provider.DocumentFile
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.acube.audii.model.compareUris
import com.acube.audii.model.database.Datasource
import com.acube.audii.repository.audioBook.DatasourceRepository
import com.acube.audii.service.PlayerService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

data class AudiobookListUiState(
    val audiobooks: StateFlow<List<Audiobook>> = MutableStateFlow(emptyList()),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AudiobookViewModel @Inject constructor(
    private val repository: AudiobookRepository,
    private val datasourceRepository: DatasourceRepository,
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    private val _audioBookUiState = MutableStateFlow(AudiobookListUiState())
    val audioBookUiState = _audioBookUiState.asStateFlow()

    private var currentPlayingAudiobook: String = ""
    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>
    private val scope = CoroutineScope(Dispatchers.IO + Job())
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

    fun setCurrentAudiobook(id: String) {
        currentPlayingAudiobook = id
    }

    fun setUpController() {
        val sessionToken = SessionToken(
            applicationContext,
            ComponentName(applicationContext, PlayerService::class.java)
        )

        mediaControllerFuture =
            MediaController.Builder(applicationContext, sessionToken).buildAsync()

        mediaControllerFuture.addListener({
            mediaController = mediaControllerFuture.get()

            startTracking()
        }, MoreExecutors.directExecutor())
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addAudiobook(audiobookData: AudiobookData,dataSourceId:String) {
        viewModelScope.launch {
            try {
                if (_audioBookUiState.value.audiobooks.value.any {
                        compareUris(it.uriString.toUri(), audiobookData.uriString.toUri())
                    }) {
                    throw Exception("Audiobook: ${audiobookData.title} already exists")
                }
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
                        narrator = audiobookData.narrator,
                        datasourceId = dataSourceId,
                    )
                )
            } catch (e: Exception) {
                _audioBookUiState.value = _audioBookUiState.value.copy(
                    errorMessage = "Failed to add audiobook : ${e.message}"
                )
            }
        }
    }

    fun deleteAudiobook(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteAudiobook(id = id)
            } catch (e: Exception) {
                _audioBookUiState.value = _audioBookUiState.value.copy(
                    errorMessage = "Failed to delete audiobook : ${e.message}"
                )
            }
        }
    }

    fun updateAudiobookSpeed(speed: Float,id:String) {
        viewModelScope.launch {
            try {
                repository.updatePlaybackSpeed(id,speed)
            } catch (e: Exception) {
                _audioBookUiState.value = _audioBookUiState.value.copy(
                    errorMessage = "Failed to update audiobook speed: ${e.message}"
                )
            }
        }
    }

    fun clearAudiobookUiStateError() {
        _audioBookUiState.value = _audioBookUiState.value.copy(
            errorMessage = null
        )
    }
    fun markAudiobookAsCompleted(id: String) {
        viewModelScope.launch {
            try {
                val audiobook = _audioBookUiState.value.audiobooks.value.find { it.id == id }
                audiobook?.let {
                    val lastTrackIndex = it.duration.size - 1
                    val lastTrackDuration = it.duration[lastTrackIndex]
                    repository.updatePlaybackPosition(id, Pair(lastTrackIndex, lastTrackDuration))
                }
            } catch (e: Exception) {
                _audioBookUiState.value = _audioBookUiState.value.copy(errorMessage = "Failed to mark audiobook as completed: ${e.message}")
            }
        }
    }
    fun updateAudiobookCollections(collectionId: Int, id: String) {
        viewModelScope.launch {
            try {
                val audiobook = _audioBookUiState.value.audiobooks.value.find { it.id == id }
                audiobook?.let {
                    val updatedCollections = it.collections.toMutableList()
                    if (!updatedCollections.contains(collectionId)) {
                        updatedCollections.add(collectionId)
                        updatedCollections.sort()
                        repository.updateAudiobookCollection(id, updatedCollections)
                    }
                }
            } catch (e: Exception) {
                _audioBookUiState.value = _audioBookUiState.value.copy(
                    errorMessage = "Failed to update audiobook collections: ${e.message}"
                )
            }
        }
    }

    private suspend fun deleteDatasource(id: String) {
        datasourceRepository.deleteDatasource(id)
    }

    fun removeCollectionFromAudiobooks(collectionId: Int) {
        viewModelScope.launch {
            try {
                val audiobooksToUpdate = _audioBookUiState.value.audiobooks.value.filter {
                    it.collections.contains(collectionId)
                }

                audiobooksToUpdate.forEach { audiobook ->
                    val updatedCollections = audiobook.collections.toMutableList()
                    updatedCollections.remove(collectionId)
                    updatedCollections.sort()
                    repository.updateAudiobookCollection(audiobook.id, updatedCollections)
                }
            } catch (e: Exception) {
                _audioBookUiState.value = _audioBookUiState.value.copy(
                    errorMessage = "Failed to remove collection from audiobooks: ${e.message}"
                )
            }
        }
    }

    fun saveAudiobookProgress() {
        viewModelScope.launch {
            try {
                repository.updatePlaybackPosition(
                    currentPlayingAudiobook, position = Pair(
                        mediaController?.currentMediaItemIndex ?: 0,
                        mediaController?.currentPosition ?: 0L
                    )
                )
            } catch (e: Exception) {
                _audioBookUiState.value = _audioBookUiState.value.copy(
                    errorMessage = "Failed to save audiobook progress: ${e.message}"
                )
            }
        }
    }
    fun restart(id: String){
        viewModelScope.launch {
            try{
                repository.updatePlaybackPosition(id, Pair(0, 0L))
            }catch (e: Exception) {
                _audioBookUiState.value = _audioBookUiState.value.copy(
                    errorMessage = "Failed to save audiobook progress: ${e.message}"
                )
            }
        }
    }
    fun syncDatasources(): List<Pair<Uri, String>> {
        _audioBookUiState.value = _audioBookUiState.value.copy(isLoading = true)
        val toReturn = mutableListOf<Pair<Uri,String>>()

        runBlocking {
            val job = viewModelScope.launch(Dispatchers.IO) {
                try {
                    val existingUriStrings =
                        async { repository.getAudiobooks().map { it.uriString }.toSet() }.await()
                    val datasources = async { datasourceRepository.getAllDatasources() }.await()

                    datasources.forEach { dataSource ->
                        val doc = if (DocumentsContract.isTreeUri(dataSource.uri.toUri())) {
                            DocumentFile.fromTreeUri(applicationContext, dataSource.uri.toUri())
                        } else {
                            DocumentFile.fromSingleUri(applicationContext, dataSource.uri.toUri())
                        }

                        if (doc?.exists() == true) {
                            if (doc.isDirectory) {
                                doc.listFiles().forEach { file2 ->
                                    if (existingUriStrings.none { existingUri ->
                                            compareUris(existingUri.toUri(), file2.uri)
                                        }) {
                                        toReturn.add(Pair(file2.uri,dataSource.id))
                                    }
                                }
                            } else {
                                deleteDatasource(dataSource.id)
                            }
                        } else {
                            deleteDatasource(dataSource.id)
                        }
                    }
                } catch (e: Exception) {
                    throw Exception("Error Syncing Data Sources")
                }
            }
            job.join()
        }
        _audioBookUiState.value = _audioBookUiState.value.copy(isLoading = false)
        return toReturn
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addDatasource(uri: String):String {
        try {
            val id=  Uuid.random().toString()
            runBlocking{
                viewModelScope.launch(Dispatchers.IO) {
                    datasourceRepository.addDatasource(
                        Datasource(
                            id = id,
                            uri = uri
                        )
                    )
                }
            }
            return id
        } catch (e: Exception) {
            throw Exception("Error adding datasource: ${e.message}")
        }
    }

    private fun startTracking() {
        scope.launch {
            while (true) {
                if (currentPlayingAudiobook.isNotEmpty()) {
                    saveAudiobookProgress()
                }
                delay(90 * 1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
        currentPlayingAudiobook = ""
        mediaController?.release()
        mediaController = null
    }
}


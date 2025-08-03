package com.acube.audii.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acube.audii.model.database.Audiobook
import com.acube.audii.model.repository.AudiobookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class AudiobookListUiState(
    val audiobooks: Flow<List<Audiobook>> = MutableStateFlow(emptyList()),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AudiobookViewModel @Inject constructor(
    private val repository: AudiobookRepository
) : ViewModel(){

    private val _audioBookUiState = MutableStateFlow(AudiobookListUiState())
    val audioBookUiState = _audioBookUiState.asStateFlow()

    init {
        loadAudioBooks()
    }

    private fun loadAudioBooks(){
        _audioBookUiState.value = _audioBookUiState.value.copy(isLoading = true)

        _audioBookUiState.value= _audioBookUiState.value.copy(
            audiobooks = repository.getAllAudiobooks(),
            isLoading = false,
            errorMessage = null
        )
    }
    @OptIn(ExperimentalUuidApi::class)
    fun addAudiobook(title:String, author:String, filePath: String, duration:List<Long>, coverImageUriPath:String=""){
        viewModelScope.launch {
            try{
                repository.addAudiobook(
                    Audiobook(
                        id = Uuid.random().toHexString(),
                        title = title,
                        author = author,
                        filePath = filePath,
                        duration = duration,
                        currentPosition = Pair(0, 0L),
                        coverImageUriPath = coverImageUriPath,
                        modifiedDate = System.currentTimeMillis()
                    )
                )
            }catch (e: Exception){
                _audioBookUiState.value = _audioBookUiState.value.copy(
                    errorMessage = "Failed to add audiobook : ${e.message}"
                )
            }
        }
    }

    fun deleteAudiobook(id:String){
        viewModelScope.launch{
            try {
                repository.deleteAudiobook(id = id)
            } catch (e: Exception) {
                _audioBookUiState.value = _audioBookUiState.value.copy(
                    errorMessage = "Failed to add audiobook : ${e.message}"
                )
            }
        }
    }

    private fun clearErrorState(){
        _audioBookUiState.value = _audioBookUiState.value.copy(
            errorMessage = null
        )
    }
}


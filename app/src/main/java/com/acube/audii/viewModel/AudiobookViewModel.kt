package com.acube.audii.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acube.audii.model.database.Audiobook
import com.acube.audii.model.repository.AudiobookRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class AudiobookListUiState(
    val audiobooks: List<Audiobook> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

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

        repository.getAllAudiobooks().map {
            _audioBookUiState.value = _audioBookUiState.value.copy(
                audiobooks = it,
                isLoading = false,
                errorMessage = null
            )
        }
    }
    @OptIn(ExperimentalUuidApi::class)
    fun addAudiobook(title:String, author:String, filePath: String, duration:List<Long>,coverImageUrl:String=""){
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
                        isCompleted = false,
                        coverImageUrl = coverImageUrl,
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


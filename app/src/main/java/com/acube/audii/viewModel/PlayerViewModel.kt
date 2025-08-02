package com.acube.audii.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acube.audii.model.database.Audiobook
import com.acube.audii.model.repository.AudiobookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val currentAudiobook: Audiobook? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Pair<Int,Long> = Pair(0,0L),
    val duration: List<Long> = longArrayOf().toList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)


class PlayerViewModel @Inject constructor(
    private val repository: AudiobookRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val audiobookId:String =savedStateHandle.get<String>("audiobookId") ?: ""

    private val _uiState = MutableStateFlow(PlayerUiState())

    init {
        loadAudiobook()
    }

    private fun loadAudiobook(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true
            )
            try {
                val audiobook = repository.getAudiobookById(audiobookId)

                if(audiobook!=null){
                    _uiState.value = _uiState.value.copy(
                        currentAudiobook = audiobook,
                        currentPosition = audiobook.currentPosition,
                        duration = audiobook.duration,
                        isLoading = false
                    )
                }else{
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Audiobook not found",
                        isLoading = false
                    )
                }
            }catch (e: Exception){
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load audiobook ${e.message}"
                )
            }
        }
    }
}
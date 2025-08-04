package com.acube.audii.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acube.audii.model.database.Audiobook
import com.acube.audii.model.repository.AudiobookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: AudiobookRepository,
): ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState

    private fun playAudiobook(audiobook: Audiobook){

    }
}
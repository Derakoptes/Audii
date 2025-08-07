package com.acube.audii.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acube.audii.model.database.Audiobook
import com.acube.audii.repository.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class PlayerUiState(
    val currentAudiobook: Audiobook? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val currentChapter: Int = 0,
    val totalChapters: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: PlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState

    init {
        setUpUIState()
    }

    private fun setUpUIState() {
        viewModelScope.launch {
            combine(
                controller.currentAudiobook,
                controller.currentPosition,
                controller.currentDuration,
                controller.currentChapter,
                controller.totalChapters,
                controller.isLoading,
                controller.isPlaying,
                controller.playbackSpeed
            ) { it ->
                PlayerUiState(
                    currentAudiobook = it[0] as Audiobook?,
                    currentPosition = it[1] as Long,
                    duration = it[2] as Long,
                    currentChapter = it[3] as Int,
                    totalChapters = it[4] as Int,
                    isLoading = it[5] as Boolean,
                    isPlaying = it[6] as Boolean,
                    playbackSpeed = it[7] as Float,
                    errorMessage = ""
                )
            }.collect { _uiState.value = it }
        }
    }

    fun playAudiobook(audiobook: Audiobook) {
        try {
            controller.playAudiobook(audiobook)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = e.message)
        }
    }

}
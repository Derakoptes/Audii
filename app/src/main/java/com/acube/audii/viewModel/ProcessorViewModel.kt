package com.acube.audii.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.acube.audii.model.AudiobookData
import com.acube.audii.model.parser.MapAudiobook
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed interface ProcessorUiState {
    object Idle : ProcessorUiState
    object Loading : ProcessorUiState
    data class Success(val audiobooks: List<String>) : ProcessorUiState
    data class Error(val message: String) : ProcessorUiState
}

@HiltViewModel
class ProcessorViewModel @Inject constructor(
    private val map : MapAudiobook,
): ViewModel(){

    private val _uiState = MutableStateFlow<ProcessorUiState>(ProcessorUiState.Idle)
    val uiState = _uiState.asStateFlow()

     fun processSingleFile(uri: Uri): AudiobookData {
        _uiState.value = ProcessorUiState.Loading
        try {
            val audiobook = map.mapFileToAudiobook(uri)
            _uiState.value = ProcessorUiState.Success(listOf(audiobook).map { it.title })
            return audiobook
        } catch (e: Exception) {
            _uiState.value = ProcessorUiState.Error(e.message ?: "An unknown error occurred")
            throw e
        }
    }

     fun processFolderForSingleAudiobook(uri: Uri): AudiobookData {
        _uiState.value = ProcessorUiState.Loading
        try {
            val audiobook = map.mapFolderToAudiobook(uri)
            _uiState.value = ProcessorUiState.Success(listOf(audiobook).map { it.title })
            return audiobook
        } catch (e: Exception) {
            _uiState.value = ProcessorUiState.Error(e.message ?: "An unknown error occurred")
            throw e
        }
    }

     fun processFolderForMultipleAudiobooks(uri: Uri): List<AudiobookData> {
        _uiState.value = ProcessorUiState.Loading
        try {
            val audiobooks = map.mapFolderToAudiobooks(uri)
            _uiState.value = ProcessorUiState.Success(audiobooks.map { it.title })
            return audiobooks
        } catch (e: Exception) {
            _uiState.value = ProcessorUiState.Error(e.message ?: "An unknown error occurred")
            throw e
        }
    }
}
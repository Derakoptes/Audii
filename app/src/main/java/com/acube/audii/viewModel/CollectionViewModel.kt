package com.acube.audii.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acube.audii.model.database.Collection
import com.acube.audii.repository.audioBook.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionListUiState(
    val collections: StateFlow<List<Collection>> = MutableStateFlow(emptyList()),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val repository: CollectionRepository
) : ViewModel() {

    private val _collectionListUiState = MutableStateFlow(CollectionListUiState())
    val collectionListUiState = _collectionListUiState.asStateFlow()

    init {
        loadCollections()
    }

    private fun loadCollections() {
        _collectionListUiState.value = _collectionListUiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                _collectionListUiState.value = _collectionListUiState.value.copy(
                    collections = repository.getAllCollections().stateIn(
                        viewModelScope,
                        SharingStarted.WhileSubscribed(5_000),
                        emptyList()
                    ),
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _collectionListUiState.value = _collectionListUiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load collections: ${e.message}"
                )
            }
        }
    }

    fun addCollection(collection: Collection) {
        viewModelScope.launch {
            try {
                if (_collectionListUiState.value.collections.value.any { it.name.equals(collection.name, ignoreCase = true) }) {
                    _collectionListUiState.value = _collectionListUiState.value.copy(
                        errorMessage = "Collection with the same name already exists"
                    )
                }
                repository.addCollection(collection)
            } catch (e: Exception) {
                _collectionListUiState.value = _collectionListUiState.value.copy(
                    errorMessage = "Failed to add collection: ${e.message}"
                )
            }
        }
    }

    fun deleteCollection(collection: Collection) {
        viewModelScope.launch {
            try {
                repository.deleteCollection(collection)
            } catch (e: Exception) {
                _collectionListUiState.value = _collectionListUiState.value.copy(
                    errorMessage = "Failed to delete collection: ${e.message}"
                )
            }
        }
    }

    fun clearErrorMessage() {
        _collectionListUiState.value = _collectionListUiState.value.copy(errorMessage = null)
    }
}
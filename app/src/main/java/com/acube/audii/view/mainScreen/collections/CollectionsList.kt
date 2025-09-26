package com.acube.audii.view.mainScreen.collections

import AudiobookList
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.acube.audii.model.database.Audiobook
import com.acube.audii.model.database.Collection
import com.acube.audii.viewModel.CollectionListUiState
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    uiState: StateFlow<CollectionListUiState>,
    clearErrorMessage: () -> Unit,
    deleteCollection: (Collection) -> Unit,
    addCollection: (Collection) -> Unit,
    audiobooks: List<Audiobook>,
    onAudiobookItemClick: (String) -> Unit,
    addAudiobookToCollection: (collectionId: Int, audiobookId: String) -> Unit,
    deleteAudiobook: (String) -> Unit,
    markAudiobookAsCompleted: (String) -> Unit
) {
    val state by uiState.collectAsState()
    val collections by state.collections.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newCollectionName by remember { mutableStateOf("") }
    var selectedCollection by remember { mutableStateOf<Collection?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var collectionToDelete by remember { mutableStateOf<Collection?>(null) }
    var showAddAudiobooksDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            if (selectedCollection != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    IconButton(onClick = { selectedCollection = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to collections")
                    }
                    Text(
                        selectedCollection!!.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                }

                val audiobooksInCollection = audiobooks.filter { isAudiobookInCollection(it, selectedCollection!!.id) }

                Spacer(modifier = Modifier.height(8.dp))
                if (audiobooksInCollection.isEmpty()) {
                    Text(
                        "No audiobooks in this collection yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                    )
                } else {
                    AudiobookList(
                        audiobooks = audiobooksInCollection,
                        onAudiobookClick = onAudiobookItemClick,
                        isCollectionScreen = true,
                        deleteAudiobook = deleteAudiobook,
                        markAudiobookAsCompleted = markAudiobookAsCompleted
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val availableAudiobooksToAdd = audiobooks.filter { audiobook ->
                    !isAudiobookInCollection(audiobook, selectedCollection!!.id)
                }

                if (availableAudiobooksToAdd.isNotEmpty()) {
                    Button(
                        onClick = { showAddAudiobooksDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("Add Audiobooks to ${selectedCollection?.name ?: "Collection"}")
                    }
                }
                else {
                    if (!state.isLoading && collections.isNotEmpty()) {
                         Text(
                            "All other audiobooks are already in this collection.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                        )
                    }
                }

            } else {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally)
                    )
                    Button(onClick = { clearErrorMessage() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Dismiss")
                    }
                }

                if (collections.isEmpty() && !state.isLoading) {
                    Text(
                        "No collections yet. Tap the '+' button to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                    )
                } else if (!state.isLoading) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(collections) { collection ->
                            CollectionItem(
                                collection = collection,
                                onDelete = {
                                    collectionToDelete = collection
                                    showDeleteConfirmDialog = true
                                },
                                onClick = { selectedCollection = collection }
                            )
                        }
                    }
                }
            }
        }

        if (selectedCollection == null) {
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Collection")
            }
        }
    }


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add New Collection") },
            text = {
                OutlinedTextField(
                    value = newCollectionName,
                    onValueChange = { newCollectionName = it },
                    label = { Text("Collection Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCollectionName.isNotBlank()) {
                            addCollection(Collection(id = 0, name = newCollectionName))
                            newCollectionName = ""
                            showDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                collectionToDelete = null
            },
            title = { Text("Delete Collection") },
            text = { Text("Are you sure you want to delete collection '${collectionToDelete?.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        collectionToDelete?.let { deleteCollection(it) }
                        showDeleteConfirmDialog = false
                        collectionToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteConfirmDialog = false
                    collectionToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddAudiobooksDialog && selectedCollection != null) {
        val availableAudiobooksToAdd = audiobooks.filter { audiobook ->
            !isAudiobookInCollection(audiobook, selectedCollection!!.id)
        }
        AddAudiobooksDialog(
            availableAudiobooks = availableAudiobooksToAdd,
            collectionName = selectedCollection!!.name,
            onDismiss = { showAddAudiobooksDialog = false },
            onConfirm = { selectedIds ->
                selectedIds.forEach { audiobookId ->
                    addAudiobookToCollection(selectedCollection!!.id, audiobookId)
                }
                showAddAudiobooksDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionItem(
    collection: Collection,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = collection.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Collection")
            }
        }
    }
}

@Composable
private fun AddAudiobooksDialog(
    availableAudiobooks: List<Audiobook>,
    collectionName: String,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var selectedAudiobookIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to '$collectionName'") },
        text = {
            if (availableAudiobooks.isEmpty()) {
                Text("No audiobooks available to add.")
            } else {
                LazyColumn {
                    items(availableAudiobooks) { audiobook ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAudiobookIds = if (selectedAudiobookIds.contains(audiobook.id)) {
                                        selectedAudiobookIds - audiobook.id
                                    } else {
                                        selectedAudiobookIds + audiobook.id
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedAudiobookIds.contains(audiobook.id),
                                onCheckedChange = { isChecked ->
                                    selectedAudiobookIds = if (isChecked) {
                                        selectedAudiobookIds + audiobook.id
                                    } else {
                                        selectedAudiobookIds - audiobook.id
                                    }
                                }
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "${audiobook.title} by ${audiobook.author}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedAudiobookIds.toList())
                },
                enabled = selectedAudiobookIds.isNotEmpty()
            ) {
                Text("Add Selected")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


private fun isAudiobookInCollection(audiobook: Audiobook, collectionId: Int): Boolean {
    // Perform a binary search on the sorted list of collection IDs
    val sortedCollections = audiobook.collections.sorted()
    var low = 0
    var high = sortedCollections.size - 1

    while (low <= high) {
        val mid = (low + high) / 2
        when {
            sortedCollections[mid] == collectionId -> return true
            sortedCollections[mid] < collectionId -> low = mid + 1
            else -> high = mid - 1
        }
    }
    return false
}

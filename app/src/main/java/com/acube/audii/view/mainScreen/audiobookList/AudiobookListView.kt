import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.acube.audii.model.database.Audiobook
import com.acube.audii.model.database.Collection
import com.acube.audii.view.mainScreen.audiobookList.AudiobookListItem
import com.acube.audii.view.mainScreen.collections.CollectionsScreen
import com.acube.audii.view.mainScreen.player.BottomPlayerSheet
import com.acube.audii.viewModel.AudiobookListUiState
import com.acube.audii.viewModel.CollectionListUiState
import com.acube.audii.viewModel.PlayerUiState
import com.acube.audii.viewModel.ProcessorUiState
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookListScreen(
    audiobooks: StateFlow<List<Audiobook>>,
    onAudiobookClick: (String) -> Unit = {},
    onAddAudiobook: () -> Unit = {},
    isAddingAudiobook: StateFlow<ProcessorUiState>,
    playerState: StateFlow<PlayerUiState>,
    onPlayerPlayPause: () -> Unit = {},
    onPlayerSkipNext: () -> Unit = {},
    onPlayerSkipPrevious: () -> Unit = {},
    onPlayerClick: () -> Unit = {},
    onSwipeDown: () -> Unit,
    audiobookUiState: StateFlow<AudiobookListUiState>,
    clearAudiobookUiStateError: () -> Unit,
    clearProcessorUiStateError: () -> Unit,
    collectionState: StateFlow<CollectionListUiState>,
    clearCollectionErrorMessage: () -> Unit,
    deleteCollection: (Collection) -> Unit,
    addCollection:(Collection)->Unit,
    addAudiobookToCollection:(collectionId:Int,audiobookId:String)->Unit
) {
    val audiobookList by audiobooks.collectAsState(initial = emptyList())
    val isAdding by isAddingAudiobook.collectAsState()
    val currentPlayerState by playerState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showCollectionsView by remember { mutableStateOf(false) }

    val filteredAudiobooks = remember(audiobookList, searchQuery) {
        if (searchQuery.isBlank()) {
            audiobookList
        } else {
            audiobookList.filter { audiobook ->
                audiobook.title.contains(searchQuery, ignoreCase = true) ||
                        audiobook.author.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (isSearchActive) {
                SearchTopBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchClose = {
                        isSearchActive = false
                        searchQuery = ""
                    }
                )
            } else {
                AudiobookTopBar(
                    audiobookCount = audiobookList.size,
                    onSearchClick = { isSearchActive = true },
                    onAddAudiobook = onAddAudiobook,
                    isShowingCollections = showCollectionsView,
                    onToggleView = { showCollectionsView = !showCollectionsView },
                )
            }
        },
        floatingActionButton = {
            if (false) {
                FloatingActionButton(
                    onClick = onAddAudiobook,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add audiobook"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isAdding==ProcessorUiState.Loading ||  audiobookUiState.value.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                ErrorSection(
                    processError = isAdding,
                    clearProcessorUiStateError = clearProcessorUiStateError,
                    audiobookUiState = audiobookUiState.value,
                    clearAudiobookUiStateError = clearAudiobookUiStateError
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            bottom = if (currentPlayerState.currentAudiobook != null) 72.dp else 0.dp
                        )
                ) {
                    if (showCollectionsView) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ){
                            CollectionsScreen(
                                uiState =collectionState,
                                clearErrorMessage =clearCollectionErrorMessage,
                                deleteCollection = deleteCollection,
                                addCollection = addCollection,
                                audiobooks = filteredAudiobooks,
                                onAudiobookItemClick = onAudiobookClick,
                                addAudiobookToCollection = addAudiobookToCollection
                            )
                        }
                    } else {
                        when {
                            audiobookList.isEmpty() -> {
                                EmptyStateContent(
                                    onAddAudiobook = onAddAudiobook,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                            filteredAudiobooks.isEmpty() && searchQuery.isNotBlank() -> {
                                NoSearchResultsContent(
                                    searchQuery = searchQuery,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                            else -> {
                                AudiobookList(
                                    audiobooks = filteredAudiobooks,
                                    onAudiobookClick = onAudiobookClick,
                                )
                            }
                        }
                    }
                }
            }
            
            if (currentPlayerState.currentAudiobook != null) {
                BottomPlayerSheet(
                    playerState = currentPlayerState,
                    onPlayPause = onPlayerPlayPause,
                    onSkipNext = onPlayerSkipNext,
                    onSkipPrevious = onPlayerSkipPrevious,
                    onPlayerClick = onPlayerClick,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(paddingValues),
                    onSwipeDown=onSwipeDown
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudiobookTopBar(
    audiobookCount: Int,
    onSearchClick: () -> Unit,
    onAddAudiobook: () -> Unit,
    isShowingCollections: Boolean,
    onToggleView: () -> Unit,
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Audii",
                    style = MaterialTheme.typography.titleLarge,
                )
                if (audiobookCount > 0 && !isShowingCollections) {
                    Text(
                        text = "$audiobookCount ${if (audiobookCount == 1) "book" else "books"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (isShowingCollections) {
                     Text(
                        text = "Collections",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            if(!isShowingCollections){
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search audiobooks"
                    )
                }
                IconButton(onClick = onAddAudiobook) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add audiobook"
                    )
                }
            }
            IconButton(onClick = onToggleView) {
                Icon(
                    imageVector = if (isShowingCollections) Icons.Filled.Menu else Icons.AutoMirrored.Filled.List,
                    contentDescription = if (isShowingCollections) "View Audiobook List" else "View Collections"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Search audiobooks...",
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onSearchClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close search"
                )
            }
        },
        actions = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search"
                    )
                }
            }
        },

    )
}

@Composable
 fun AudiobookList(
    audiobooks: List<Audiobook>,
    onAudiobookClick: (String) -> Unit,
    isCollectionScreen: Boolean = false
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val recentlyPlayed = audiobooks
            .filter {
                (it.currentPosition.first > 0 || it.currentPosition.second > 0)
            }
            .sortedByDescending { it.modifiedDate }
            .take(2)

        if (recentlyPlayed.isNotEmpty()) {
            item {
                SectionHeader(title = "Continue Listening")
            }
            items(recentlyPlayed) { audiobook ->
                AudiobookListItem(
                    audiobook = audiobook,
                    onPlayClick =  { onAudiobookClick(audiobook.id) },
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        val remainingAudiobooks =  audiobooks - recentlyPlayed.toSet()
        if (remainingAudiobooks.isNotEmpty()) {
            item {
                SectionHeader(
                    title = if (recentlyPlayed.isNotEmpty() && !isCollectionScreen ) "All Audiobooks" else "Audiobooks"
                )
            }
            items(remainingAudiobooks.sortedBy { it.title }) { audiobook ->
                AudiobookListItem(
                    audiobook = audiobook,
                    onPlayClick = { onAudiobookClick(audiobook.id) },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun EmptyStateContent(
    onAddAudiobook: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "No audiobooks yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Add your first audiobook to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onAddAudiobook,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Audiobook")
        }
    }
}

@Composable
private fun NoSearchResultsContent(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "No results found",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = "No audiobooks match \"$searchQuery\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorSection(
    processError: ProcessorUiState,
    clearProcessorUiStateError: () -> Unit,
    audiobookUiState: AudiobookListUiState,
    clearAudiobookUiStateError: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        if (processError is ProcessorUiState.Error) {
            ErrorCard(
                errorMessage = processError.message,
                onDismiss = clearProcessorUiStateError
            )
        }
        if (audiobookUiState.errorMessage != null) {
            ErrorCard(
                errorMessage = audiobookUiState.errorMessage,
                onDismiss = clearAudiobookUiStateError
            )
        }
    }
}

@Composable
private fun ErrorCard(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = errorMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(0.75f))
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Clear, contentDescription = "Dismiss error", tint = MaterialTheme.colorScheme.error,modifier = Modifier.weight(0.75f))
            }
        }
    }
}

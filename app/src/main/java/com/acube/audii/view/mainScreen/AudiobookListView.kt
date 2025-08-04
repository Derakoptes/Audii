import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.acube.audii.model.database.Audiobook
import com.acube.audii.ui.theme.AudiiTheme
import com.acube.audii.view.mainScreen.AudiobookListItem
import com.acube.audii.viewModel.ProcessorUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf


private val sampleAudiobooks = listOf(
    Audiobook(
        id = "1",
        title = "The Hobbit",
        author = "J.R.R. Tolkien",
        filePath = "/path/to/hobbit.mp3",
        duration = listOf(3600000L, 3900000L, 4200000L),
        currentPosition = Pair(1, 1800000L),
        coverImageUriPath = null,
        modifiedDate = System.currentTimeMillis() - 86400000L,
        narrator = "John Doe"
    ),
    Audiobook(
        id = "2",
        title = "Dune",
        author = "Frank Herbert",
        filePath = "/path/to/dune.mp3",
        duration = listOf(5400000L, 6000000L, 5700000L, 6300000L),
        currentPosition = Pair(3, 4500000L),
        coverImageUriPath = "https://example.com/dune-cover.jpg",
        modifiedDate = System.currentTimeMillis() - 172800000L,
        narrator = "John Doe"

    ),
    Audiobook(
        id = "3",
        title = "1984",
        author = "George Orwell",
        filePath = "/path/to/1984.mp3",
        duration = listOf(4200000L, 3900000L, 4500000L),
        currentPosition = Pair(2, 4500000L),
        coverImageUriPath = null,
        modifiedDate = System.currentTimeMillis() - 259200000L,
        narrator = "John Doe"
    ),
    Audiobook(
        id = "4",
        title = "The Martian",
        author = "Andy Weir",
        filePath = "/path/to/martian.mp3",
        duration = listOf(3300000L, 3600000L, 3900000L, 3300000L),
        currentPosition = Pair(0, 0L), // Not started
        coverImageUriPath = "https://example.com/martian-cover.jpg",
        modifiedDate = System.currentTimeMillis() - 345600000L,
        narrator = "John Doe"
    ),
    Audiobook(
        id = "5",
        title = "Atomic Habits",
        author = "James Clear",
        filePath = "/path/to/atomic-habits.mp3",
        duration = listOf(2700000L, 3000000L, 2400000L, 3300000L, 2700000L), // 5 chapters
        currentPosition = Pair(2, 1200000L), // Chapter 3, 20 minutes in
        coverImageUriPath = null,
        modifiedDate = System.currentTimeMillis() - 432000000L, // 5 days ago,
        narrator = "John Doe"

    )
)

@Preview(showBackground = true)
@Composable
private fun AudiobookListScreenPreview() {
    AudiiTheme {
        AudiobookListScreen(
            audiobooks = MutableStateFlow(sampleAudiobooks) as StateFlow<List<Audiobook>>,
            isAddingAudiobook = MutableStateFlow(ProcessorUiState.Idle)

        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AudiobookListScreenEmptyPreview() {
    AudiiTheme {
        AudiobookListScreen(
            audiobooks = MutableStateFlow(emptyList()),
            isAddingAudiobook = MutableStateFlow(ProcessorUiState.Idle)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AudiobookListScreenSingleItemPreview() {
    AudiiTheme {
        AudiobookListScreen(
            audiobooks = MutableStateFlow(listOf(sampleAudiobooks.first())),
            isAddingAudiobook = MutableStateFlow(ProcessorUiState.Idle)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AudiobookListScreenWithLoadingPreview() {
    AudiiTheme {
        AudiobookListScreen(
            audiobooks = MutableStateFlow(sampleAudiobooks),
            isAddingAudiobook = MutableStateFlow(ProcessorUiState.Loading)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookListScreen(
    audiobooks: StateFlow<List<Audiobook>>,
    onAudiobookClick: (String) -> Unit = {},
    onAddAudiobook: () -> Unit = {},
    isAddingAudiobook: StateFlow<ProcessorUiState>
) {
    val audiobookList by audiobooks.collectAsState(initial = emptyList())
    val isAdding by isAddingAudiobook.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

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
                    onAddAudiobook = onAddAudiobook
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
        Column( // Use Column to stack content vertically
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isAdding==ProcessorUiState.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Box( // Wrap the existing content in a Box or similar if needed for alignment
                modifier = Modifier
                    .fillMaxSize()
            ) {
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
                            onAudiobookClick = onAudiobookClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudiobookTopBar(
    audiobookCount: Int,
    onSearchClick: () -> Unit,
    onAddAudiobook: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Audii",
                    style = MaterialTheme.typography.titleLarge,
                )
                if (audiobookCount > 0) {
                    Text(
                        text = "$audiobookCount ${if (audiobookCount == 1) "book" else "books"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
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
private fun AudiobookList(
    audiobooks: List<Audiobook>,
    onAudiobookClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Recently played section
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
                    onClick = { onAudiobookClick(audiobook.id) },
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // All audiobooks section
        val remainingAudiobooks = audiobooks - recentlyPlayed.toSet()
        if (remainingAudiobooks.isNotEmpty()) {
            item {
                SectionHeader(
                    title = if (recentlyPlayed.isNotEmpty()) "All Audiobooks" else "Audiobooks"
                )
            }
            items(remainingAudiobooks.sortedBy { it.title }) { audiobook ->
                AudiobookListItem(
                    audiobook = audiobook,
                    onClick = { onAudiobookClick(audiobook.id) },
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
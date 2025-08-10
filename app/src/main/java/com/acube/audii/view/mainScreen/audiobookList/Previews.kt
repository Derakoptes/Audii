package com.acube.audii.view.mainScreen.audiobookList

import AudiobookListScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.acube.audii.model.database.Audiobook
import com.acube.audii.ui.theme.AudiiTheme
import com.acube.audii.viewModel.AudiobookListUiState
import com.acube.audii.viewModel.PlayerUiState
import com.acube.audii.viewModel.ProcessorUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.System.currentTimeMillis

private val sampleAudiobooks = listOf(
    Audiobook(
        id = "1",
        title = "The Hobbit",
        author = "J.R.R. Tolkien",
        uriString = "/path/to/hobbit.mp3",
        duration = listOf(3600000L, 3900000L, 4200000L),
        currentPosition = Pair(1, 1800000L),
        coverImageUriPath = null,
        modifiedDate = currentTimeMillis() - 86400000L,
        narrator = "John Doe"
    ),
    Audiobook(
        id = "2",
        title = "Dune",
        author = "Frank Herbert",
        uriString = "/path/to/dune.mp3",
        duration = listOf(5400000L, 6000000L, 5700000L, 6300000L),
        currentPosition = Pair(3, 4500000L),
        coverImageUriPath = "https://example.com/dune-cover.jpg",
        modifiedDate = currentTimeMillis() - 172800000L,
        narrator = "John Doe"

    ),
    Audiobook(
        id = "3",
        title = "1984",
        author = "George Orwell",
        uriString = "/path/to/1984.mp3",
        duration = listOf(4200000L, 3900000L, 4500000L),
        currentPosition = Pair(2, 4500000L),
        coverImageUriPath = null,
        modifiedDate = currentTimeMillis() - 259200000L,
        narrator = "John Doe"
    ),
    Audiobook(
        id = "4",
        title = "The Martian",
        author = "Andy Weir",
        uriString = "/path/to/martian.mp3",
        duration = listOf(3300000L, 3600000L, 3900000L, 3300000L),
        currentPosition = Pair(0, 0L), // Not started
        coverImageUriPath = "https://example.com/martian-cover.jpg",
        modifiedDate = currentTimeMillis() - 345600000L,
        narrator = "John Doe"
    ),
    Audiobook(
        id = "5",
        title = "Atomic Habits",
        author = "James Clear",
        uriString = "/path/to/atomic-habits.mp3",
        duration = listOf(2700000L, 3000000L, 2400000L, 3300000L, 2700000L), // 5 chapters
        currentPosition = Pair(2, 1200000L), // Chapter 3, 20 minutes in
        coverImageUriPath = null,
        modifiedDate = currentTimeMillis() - 432000000L, // 5 days ago,
        narrator = "John Doe"

    )
)

@Preview(showBackground = true)
@Composable
private fun AudiobookListScreenPreview() {
    AudiiTheme {
        AudiobookListScreen(
            audiobooks = MutableStateFlow(sampleAudiobooks) as StateFlow<List<Audiobook>>,
            isAddingAudiobook = MutableStateFlow(ProcessorUiState.Idle),
            playerState = MutableStateFlow(PlayerUiState()),
            onSwipeDown = {},
            audiobookUiState = MutableStateFlow(AudiobookListUiState()),
            clearAudiobookUiStateError = {},
            clearProcessorUiStateError = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AudiobookListScreenEmptyPreview() {
    AudiiTheme {
        AudiobookListScreen(
            audiobooks = MutableStateFlow(emptyList()),
            isAddingAudiobook = MutableStateFlow(ProcessorUiState.Idle),
            playerState = MutableStateFlow(PlayerUiState()),
            onSwipeDown = {},
            audiobookUiState = MutableStateFlow(AudiobookListUiState()),
            clearAudiobookUiStateError = {},
            clearProcessorUiStateError = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AudiobookListScreenSingleItemPreview() {
    AudiiTheme {
        AudiobookListScreen(
            audiobooks = MutableStateFlow(listOf(sampleAudiobooks.first())),
            isAddingAudiobook = MutableStateFlow(ProcessorUiState.Idle),
            playerState = MutableStateFlow(PlayerUiState()),
            onSwipeDown = {},
            audiobookUiState = MutableStateFlow(AudiobookListUiState()),
            clearAudiobookUiStateError = {},
            clearProcessorUiStateError = {}

            )
    }
}

@Preview(showBackground = true)
@Composable
private fun AudiobookListScreenWithLoadingPreview() {
    AudiiTheme {
        AudiobookListScreen(
            audiobooks = MutableStateFlow(sampleAudiobooks),
            isAddingAudiobook = MutableStateFlow(ProcessorUiState.Loading),
            playerState = MutableStateFlow(PlayerUiState()),
            onSwipeDown = {},
            audiobookUiState = MutableStateFlow(AudiobookListUiState()),
            clearAudiobookUiStateError = {},
            clearProcessorUiStateError = {}
            )
    }
}


@Preview
@Composable
fun PreviewAudiobookListItem() {
    AudiobookListItem(
        audiobook = Audiobook(
            id = "1",
            title = "Sample Audiobook",
            author = "Sample Author",
            uriString = "/path/to/sample.mp3",
            duration = listOf(120000L, 150000L, 180000L),
            currentPosition = Pair(0, 120000),
            coverImageUriPath = null,
            modifiedDate = currentTimeMillis(),
            narrator = "Sample Narrator"
        ),
        onPlayClick = {},
    )
}


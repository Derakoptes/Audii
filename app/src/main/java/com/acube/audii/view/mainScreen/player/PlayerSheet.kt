package com.acube.audii.view.mainScreen.player

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.acube.audii.model.database.Audiobook
import com.acube.audii.model.getImageFromPath
import com.acube.audii.repository.player.Chapter
import com.acube.audii.viewModel.PlayerUiState
import java.util.Locale

@SuppressLint("UnusedBoxWithConstraintsScope","UnusedMaterial3ScaffoldPaddingParameter",
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSheet(
    playerState: PlayerUiState,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onClose: () -> Unit,
    onGoToChapter: (Int) -> Unit,
    onChangeSpeed: (Float) -> Unit,
    formatTime: (Long) -> String,
    modifier: Modifier = Modifier
) {
    if (playerState.currentAudiobook == null) return

    var isUserDragging by remember { mutableStateOf(false) }
    var userSeekPosition by remember { mutableFloatStateOf(0f) }
    var showingScreen by remember { mutableStateOf(PlayerSheetScreen.IMAGE) }

    var playbackSpeed by remember { mutableFloatStateOf(playerState.playbackSpeed) }

     fun changeSpeed(){
         val newSpeed = if(playbackSpeed == 3f){
             0.5f
         }else{
             playbackSpeed+0.5f
         }
        playbackSpeed = newSpeed
        onChangeSpeed(newSpeed)
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) {  paddingValues ->
        Surface(
            modifier = modifier.padding(vertical = 10.dp, horizontal = 15.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = playerState.currentAudiobook.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "by ${playerState.currentAudiobook.author}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Close player"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                BoxWithConstraints {
                    val isPortrait = maxHeight > maxWidth
                    val coverArtWidth= (maxWidth.value *0.7f).dp
                    val coverArtHeight= (maxHeight.value *0.6f).dp

                    if (isPortrait) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {

                                when(showingScreen){
                                    PlayerSheetScreen.CHAPTERS -> {
                                       AnimatedVisibility(
                                           visible = true,
                                           enter = slideInHorizontally(tween(1000)),
                                           exit = slideOutHorizontally(tween(1000))
                                       ) {
                                            ChapterListDialog(
                                                chapters = playerState.chapters,
                                                currentChapter = playerState.currentChapter,
                                                onChapterSelected = {
                                                    onGoToChapter(it)
                                                },
                                                modifier = Modifier
                                                    .pointerInput(Unit) {
                                                        detectHorizontalDragGestures { change, dragAmount ->
                                                            if (dragAmount > 0) {
                                                                showingScreen =
                                                                    PlayerSheetScreen.IMAGE
                                                            }
                                                        }
                                                    }
                                                    .size(coverArtWidth)
                                            )
                                        }
                                    }
                                    PlayerSheetScreen.IMAGE -> {
                                        AnimatedVisibility(
                                            visible=true,
                                            enter = slideInHorizontally(tween(1000)),
                                            exit = slideOutHorizontally(tween(1000))
                                        ){
                                            Card(
                                                modifier = Modifier.size(coverArtWidth),
                                                shape = RoundedCornerShape(16.dp),
                                                elevation = CardDefaults.cardElevation(
                                                    defaultElevation = 8.dp
                                                )
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                        .pointerInput(Unit) {
                                                            detectHorizontalDragGestures { change, dragAmount ->
                                                                if (dragAmount < 0) {
                                                                    showingScreen =
                                                                        PlayerSheetScreen.CHAPTERS
                                                                }
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    playerState.currentAudiobook.coverImageUriPath?.let { coverPath ->
                                                        val bitmap = getImageFromPath(coverPath)
                                                        bitmap?.let {
                                                            Image(
                                                                bitmap = it.asImageBitmap(),
                                                                contentDescription = "Cover art",
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        }
                                                    } ?: run {
                                                        Text(
                                                            text = playerState.currentAudiobook.title.take(
                                                                2
                                                            )
                                                                .uppercase(),
                                                            style = MaterialTheme.typography.displayLarge,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = playerState.currentAudiobook.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "by ${playerState.currentAudiobook.author}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                if (playerState.totalChapters > 1) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Chapter ${playerState.currentChapter + 1} of ${playerState.totalChapters}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Progress section
                            Column(Modifier.fillMaxWidth()) {
                                // Seek bar
                                Slider(
                                    value = if (isUserDragging) userSeekPosition else {
                                        if (playerState.duration > 0) {
                                            (playerState.currentPosition.toFloat() / playerState.duration.toFloat()).coerceIn(
                                                0f,
                                                1f
                                            )
                                        } else 0f
                                    },
                                    onValueChange = { value ->
                                        isUserDragging = true
                                        userSeekPosition = value
                                    },
                                    onValueChangeFinished = {
                                        isUserDragging = false
                                        val seekPosition =
                                            (userSeekPosition * playerState.duration).toLong()
                                        onSeekTo(seekPosition)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = formatTime(playerState.currentPosition),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatTime(playerState.duration),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Control buttons
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Main controls row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    IconButton(
                                        onClick = onSkipBackward,
                                        modifier = Modifier
                                            .weight(1f)
                                            .widthIn(max = 32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Skip backward ${playerState.currentAudiobook.skipTimings.second} seconds",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .graphicsLayer { scaleX = -1f },
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Previous chapter
                                    IconButton(
                                        onClick = onSkipPrevious,
                                        enabled = playerState.currentChapter > 0 || playerState.currentPosition > 0,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                            contentDescription = "Previous chapter",
                                            modifier = Modifier.fillMaxWidth(),
                                            tint = if (playerState.currentChapter > 0 || playerState.currentPosition > 0) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            }
                                        )
                                    }

                                    Button(
                                        onClick = onPlayPause,
                                        modifier = Modifier.size(72.dp),
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        when (playerState.isPlaying) {
                                            false -> {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Play",
                                                    modifier = Modifier,
                                                    tint = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                            true ->{
                                                Image(
                                                    painter = painterResource(id = com.acube.audii.R.drawable.pause),
                                                    contentDescription = "Pause",
                                                    modifier = Modifier,
                                                    colorFilter = ColorFilter.tint(color= MaterialTheme.colorScheme.onPrimary)
                                                )
                                            }
                                        }
                                    }
                                    // Next chapter
                                    IconButton(
                                        onClick = onSkipNext,
                                        enabled = playerState.currentChapter < playerState.totalChapters - 1,
                                        modifier = Modifier
                                            .weight(1f)
                                            .widthIn(56.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Next chapter",
                                            modifier = Modifier.fillMaxWidth(),
                                            tint = if (playerState.currentChapter < playerState.totalChapters - 1) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            }
                                        )
                                    }

                                    // Skip forward
                                    IconButton(
                                        onClick = onSkipForward,
                                        modifier = Modifier
                                            .weight(1f)
                                            .widthIn(max = 32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Skip forward ${playerState.currentAudiobook.skipTimings.first} seconds",
                                            modifier = Modifier.fillMaxWidth(),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(onClick = { changeSpeed() }) {
                                    Text(text = "${playbackSpeed}x")
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            when(showingScreen){
                                PlayerSheetScreen.CHAPTERS -> {
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = slideInHorizontally(tween(1000)),
                                        exit = slideOutHorizontally(tween(1000))
                                    ) {
                                        ChapterListDialog(
                                            chapters = playerState.chapters,
                                            currentChapter = playerState.currentChapter,
                                            onChapterSelected = {
                                                onGoToChapter(it)
                                            },
                                            modifier = Modifier
                                                .pointerInput(Unit) {
                                                    detectHorizontalDragGestures { change, dragAmount ->
                                                        if (dragAmount > 0) {
                                                            showingScreen =
                                                                PlayerSheetScreen.IMAGE
                                                        }
                                                    }
                                                }
                                                .size(coverArtHeight)
                                        )
                                    }
                                }
                                PlayerSheetScreen.IMAGE -> {
                                    AnimatedVisibility(
                                        visible=true,
                                        enter = slideInHorizontally(tween(1000)),
                                        exit = slideOutHorizontally(tween(1000))
                                    ){
                                        Card(
                                            modifier = Modifier.size(coverArtHeight),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(
                                                defaultElevation = 8.dp
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .pointerInput(Unit) {
                                                        detectHorizontalDragGestures { change, dragAmount ->
                                                            if (dragAmount < 0) {
                                                                showingScreen =
                                                                    PlayerSheetScreen.CHAPTERS
                                                            }
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                playerState.currentAudiobook.coverImageUriPath?.let { coverPath ->
                                                    val bitmap = getImageFromPath(coverPath)
                                                    bitmap?.let {
                                                        Image(
                                                            bitmap = it.asImageBitmap(),
                                                            contentDescription = "Cover art",
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                } ?: run {
                                                    Text(
                                                        text = playerState.currentAudiobook.title.take(
                                                            2
                                                        )
                                                            .uppercase(),
                                                        style = MaterialTheme.typography.displayLarge,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                }
                            }
                            }

                            Spacer(modifier = Modifier.width(32.dp))

                            // Controls and info column
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Audiobook info
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = playerState.currentAudiobook.title,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "by ${playerState.currentAudiobook.author}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    if (playerState.totalChapters > 1) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Chapter ${playerState.currentChapter + 1} of ${playerState.totalChapters}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                Column(Modifier.fillMaxWidth()) {
                                    Slider(
                                        value = if (isUserDragging) userSeekPosition else {
                                            if (playerState.duration > 0) {
                                                (playerState.currentPosition.toFloat() / playerState.duration.toFloat()).coerceIn(
                                                    0f,
                                                    1f
                                                )
                                            } else 0f
                                        },
                                        onValueChange = { value ->
                                            isUserDragging = true
                                            userSeekPosition = value
                                        },
                                        onValueChangeFinished = {
                                            isUserDragging = false
                                            val seekPosition =
                                                (userSeekPosition * playerState.duration).toLong()
                                            onSeekTo(seekPosition)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = formatTime(playerState.currentPosition),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatTime(playerState.duration),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        IconButton(
                                            onClick = onSkipBackward,
                                            modifier = Modifier.size(56.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Skip backward ${playerState.currentAudiobook.skipTimings.second} seconds",
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .graphicsLayer {
                                                        scaleX = -1f
                                                    },
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Previous chapter
                                        IconButton(
                                            onClick = onSkipPrevious,
                                            enabled = playerState.currentChapter > 0 || playerState.currentPosition > 0,
                                            modifier = Modifier.size(56.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                contentDescription = "Previous chapter",
                                                modifier = Modifier.size(32.dp),
                                                tint = if (playerState.currentChapter > 0 || playerState.currentPosition > 0) {
                                                    MaterialTheme.colorScheme.onSurface
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.38f
                                                    )
                                                }
                                            )
                                        }

                                        // Play/Pause button
                                        Button(
                                            onClick = onPlayPause,
                                            modifier = Modifier.size(72.dp),
                                            shape = CircleShape,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            when (playerState.isPlaying) {
                                                false -> {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Play",
                                                        modifier = Modifier,
                                                        tint = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                }
                                                true ->{
                                                    Image(
                                                        painter = painterResource(id = com.acube.audii.R.drawable.pause),
                                                        contentDescription = "Pause",
                                                        modifier = Modifier,
                                                        colorFilter = ColorFilter.tint(color= MaterialTheme.colorScheme.onPrimary)
                                                    )
                                                }
                                            }
                                        }

                                        // Next chapter
                                        IconButton(
                                            onClick = onSkipNext,
                                            enabled = playerState.currentChapter < playerState.totalChapters - 1,
                                            modifier = Modifier.size(56.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = "Next chapter",
                                                modifier = Modifier.size(32.dp),
                                                tint = if (playerState.currentChapter < playerState.totalChapters - 1) {
                                                    MaterialTheme.colorScheme.onSurface
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.38f
                                                    )
                                                }
                                            )
                                        }

                                        // Skip forward
                                        IconButton(
                                            onClick = onSkipForward,
                                            modifier = Modifier.size(56.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Skip forward ${playerState.currentAudiobook.skipTimings.first} seconds",
                                                modifier = Modifier.size(32.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    TextButton(onClick = { changeSpeed() }) {
                                        Text(text = "${playbackSpeed}x")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterListDialog(
    chapters: List<Chapter>,
    currentChapter: Int,
    onChapterSelected: (Int) -> Unit,
    modifier: Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            itemsIndexed(chapters) { index, chapter ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChapterSelected(index) }
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index == currentChapter) MaterialTheme.colorScheme.primaryContainer else Color(0xffbdbdbd)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "${index + 1}. ${chapter.title}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (index == currentChapter) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == currentChapter) MaterialTheme.colorScheme.onPrimaryContainer else Color.Black
                        )
                    }
                }
            }
        }
    }
}
enum class PlayerSheetScreen{
    CHAPTERS,
    IMAGE
}
@Preview(showBackground = true)
@Composable
private fun PlayerSheetPreview() {
    val sampleAudiobook = Audiobook(
        id = "1",
        title = "The Hobbit",
        author = "J.R.R. Tolkien",
        narrator = "Andy Serkis",
        uriString = "/path/to/hobbit.mp3",
        duration = listOf(3600000L, 3900000L, 4200000L),
        currentPosition = Pair(1, 1800000L),
        coverImageUriPath = null,
        modifiedDate = System.currentTimeMillis()
    )

    val playerState = PlayerUiState(
        currentAudiobook = sampleAudiobook,
        isPlaying = true,
        currentPosition = 1800000L,
        duration = 3900000L,
        currentChapter = 1,
        totalChapters = 3,
        playbackSpeed = 1.0f,
        isLoading = false,
        errorMessage = null,
        chapters = listOf(
            Chapter("Chapter 1", 3600000L),
            Chapter("Chapter 2", 3900000L),
            Chapter("Chapter 3", 4200000L)
        )
    )

    MaterialTheme {
        PlayerSheet(
            playerState = playerState,
            onPlayPause = {},
            onSkipNext = {},
            onSkipPrevious = {},
            onSkipForward = {},
            onSkipBackward = {},
            onSeekTo = {},
            onClose = {},
            onChangeSpeed = {},
            formatTime = { time ->
                val totalSeconds = time / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
            },
            onGoToChapter = {}
        )
    }
}

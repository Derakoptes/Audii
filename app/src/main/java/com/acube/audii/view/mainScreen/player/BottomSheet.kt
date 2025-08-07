package com.acube.audii.view.mainScreen.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.PlayArrow

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.acube.audii.model.database.Audiobook
import com.acube.audii.model.getImageFromPath
import com.acube.audii.viewModel.PlayerUiState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource

@Composable
fun BottomPlayerSheet(
    playerState: PlayerUiState,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onPlayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (playerState.currentAudiobook == null) return
    println(playerState.toString())
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPlayerClick() }
        ,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column {
            LinearProgressIndicator(
                progress = { 
                    if (playerState.duration > 0) {
                        (playerState.currentPosition.toFloat() / playerState.duration.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        playerState.currentAudiobook.coverImageUriPath?.let { coverPath ->
                            val bitmap = getImageFromPath(coverPath)
                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "Cover",
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = playerState.currentAudiobook.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (playerState.totalChapters > 1) {
                            Text(
                                text = "Chapter ${playerState.currentChapter + 1} of ${playerState.totalChapters}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onSkipPrevious,
                        enabled = playerState.currentChapter > 0 || playerState.currentPosition > 0
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous chapter",
                            tint = if (playerState.currentChapter > 0 || playerState.currentPosition > 0) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                    
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(48.dp)
                    ) {
                        when (playerState.isPlaying) {
                            false -> {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    modifier =  Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            true ->{
                                Image(
                                    painter = painterResource(id = com.acube.audii.R.drawable.pause),
                                    contentDescription = "Pause",
                                    modifier =  Modifier.size(32.dp),
                                    colorFilter = ColorFilter.tint(color= MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                    
                    IconButton(
                        onClick = onSkipNext,
                        enabled = playerState.currentChapter < playerState.totalChapters - 1
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next chapter",
                            tint = if (playerState.currentChapter < playerState.totalChapters - 1) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomPlayerSheetPreview() {
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
        errorMessage = null
    )
    
    MaterialTheme {
        BottomPlayerSheet(
            playerState = playerState,
            onPlayPause = {},
            onSkipNext = {},
            onSkipPrevious = {},
            onPlayerClick = {}
        )
    }
}

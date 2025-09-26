package com.acube.audii.view.mainScreen.audiobookList

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acube.audii.model.database.Audiobook
import com.acube.audii.model.getImageFromPath
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AudiobookListItem(
    audiobook: Audiobook,
    modifier: Modifier = Modifier,
    onPlayClick: (String) -> Unit,
    deleteAudiobook: (String) -> Unit,
    markAudiobookAsCompleted: (String) -> Unit
) {
    var showModalSheet by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onPlayClick(audiobook.id) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showModalSheet = true
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AudiobookCover(
                coverImagePath = audiobook.coverImageUriPath,
                title = audiobook.title,
                isCompleted = isCompleted(audiobook),
                modifier = Modifier.size(70.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = audiobook.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "by ${audiobook.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                ProgressSection(audiobook = audiobook)

                AudiobookProgressBar(audiobook = audiobook)
            }
        }
    }

    if (showModalSheet) {
        AudiobookDetailSheet(
            audiobook = audiobook,
            onDismiss = { showModalSheet = false },
            sheetState = sheetState,
            deleteAudiobook = deleteAudiobook,
            markAudiobookAsCompleted = markAudiobookAsCompleted,
        )
    }
}

@Composable
fun AudiobookDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookDetailSheet(
    audiobook: Audiobook,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    deleteAudiobook: (String) -> Unit,
    markAudiobookAsCompleted: (String) -> Unit
) {
    val progressInfo = remember(audiobook) {
        calculateProgressInfo(audiobook)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                AudiobookCover(
                    coverImagePath = audiobook.coverImageUriPath,
                    title = audiobook.title,
                    isCompleted = isCompleted(audiobook),
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = audiobook.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "by ${audiobook.author}",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    AudiobookDetailRow(label = "Narrator", value = audiobook.narrator)
                    AudiobookDetailRow(label = "Duration", value = progressInfo.totalTimeFormatted)
                    AudiobookDetailRow(
                        label = "Progress",
                        value = "${progressInfo.overallProgress}% (${progressInfo.currentTimeFormatted})"
                    )

                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    if (audiobook.datasourceId.isEmpty()){
                        deleteAudiobook(audiobook.id)
                    }else{
                        deleteAudiobook("")
                    }
                },
                    ) {
                    Text("Delete")
                }
                IconButton(onClick = {
                        markAudiobookAsCompleted(audiobook.id)
                }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark as completed"
                    )
                }
            }
        }
    }
}


@Composable
private fun AudiobookCover(
    coverImagePath: String?,
    title: String,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val image = coverImagePath?.let { getImageFromPath(coverImagePath) }
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (coverImagePath != null && image != null) {
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = "$title cover",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "No cover",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (isCompleted) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 3.dp, y = (-3).dp)
                    .size(16.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun ProgressSection(audiobook: Audiobook) {
    val (currentChapterIndex, currentChapterTimeMillis) = audiobook.currentPosition
    val totalChapters = audiobook.duration.size

    val progressInfo = remember(audiobook) {
        calculateProgressInfo(audiobook)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!(currentChapterIndex == 0 && totalChapters == 1)) {
            Text(
                text = "Ch. ${currentChapterIndex + 1}/$totalChapters",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier)
        }

        Text(
            text = "${progressInfo.overallProgress}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AudiobookProgressBar(audiobook: Audiobook) {
    val progressInfo = remember(audiobook) {
        calculateProgressInfo(audiobook)
    }

    LinearProgressIndicator(
        progress = { progressInfo.overallProgress / 100f },
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(1.5.dp)),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.primaryContainer,
    )
}

private data class ProgressInfo(
    val currentTimeFormatted: String,
    val totalTimeFormatted: String,
    val overallProgress: Int
)

private fun calculateProgressInfo(audiobook: Audiobook): ProgressInfo {
    val totalDurationMillis = audiobook.duration.sum()
    val currentTotalMillis = audiobook.duration.take(audiobook.currentPosition.first).sum() +
            audiobook.currentPosition.second

    val overallProgress = if (totalDurationMillis > 0) {
        ((currentTotalMillis.toFloat() / totalDurationMillis) * 100).toInt()
    } else 0

    return ProgressInfo(
        currentTimeFormatted = formatDuration(currentTotalMillis),
        totalTimeFormatted = formatDuration(totalDurationMillis),
        overallProgress = overallProgress
    )
}

private fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

private fun isCompleted(audiobook: Audiobook): Boolean {
    val totalDurationMillis = audiobook.duration.sum()
    val currentTotalMillis = audiobook.duration.take(audiobook.currentPosition.first).sum() +
            audiobook.currentPosition.second

    val completionThresholdPercentage = 0.98

    return if (totalDurationMillis > 0) {
        (currentTotalMillis.toDouble() / totalDurationMillis) >= completionThresholdPercentage
    } else false
}

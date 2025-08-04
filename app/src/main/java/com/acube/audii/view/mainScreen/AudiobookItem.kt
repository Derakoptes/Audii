package com.acube.audii.view.mainScreen


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import java.lang.System.currentTimeMillis
import java.util.Locale
import java.util.concurrent.TimeUnit

@Preview
@Composable
fun PreviewAudiobookListItem() {
         AudiobookListItem(
             audiobook = Audiobook(
                 id = "1",
                 title = "Sample Audiobook",
                 author = "Sample Author",
                 uriString = "/path/to/sample.mp3",
                 duration = listOf(120000L, 150000L, 180000L), // Example durations for chapters
                 currentPosition = Pair(0, 120000),
                 coverImageUriPath = null,
                 modifiedDate = currentTimeMillis(),
                 narrator = "Sample Narrator"
             ),
             onPlayClick = {}
         )
     }


@Composable
fun AudiobookListItem(
    audiobook: Audiobook,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onPlayClick: ((String) -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(0.95f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover Image Section
            AudiobookCover(
                coverImagePath = audiobook.coverImageUriPath,
                title = audiobook.title,
                isCompleted = isCompleted(audiobook),
                modifier = Modifier.size(70.dp)
            )

            // Content Section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Title
                Text(
                    text = audiobook.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )

                // Author
                Text(
                    text = "by ${audiobook.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Progress Information
                ProgressSection(audiobook = audiobook)

                // Progress Bar
                AudiobookProgressBar(audiobook = audiobook)
            }
            if (onPlayClick != null) {
                IconButton(onClick = { onPlayClick(audiobook.id) }) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.primary
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
                if(coverImagePath!=null&&image!=null){
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = "$title cover",
                        modifier = Modifier.fillMaxSize()
                    )
                }else {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "No cover",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
        }

        // Completion badge
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
        if (!(currentChapterIndex==0 && totalChapters==1)){
            Text(
                text = "Ch. ${currentChapterIndex + 1}/$totalChapters",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }else{
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
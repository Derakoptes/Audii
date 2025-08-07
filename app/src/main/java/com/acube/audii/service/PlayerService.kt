package com.acube.audii.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.acube.audii.R
import com.acube.audii.model.database.Audiobook
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val binder = PlayerBinder()

    inner class PlayerBinder : Binder()

    override fun onBind(intent: Intent?): IBinder {
        return super.onBind(intent) ?: binder
    }

    companion object {
        const val ACTION_PLAY = "com.acube.audii.service.PLAY"
        const val EXTRA_AUDIOBOOK = "com.acube.audii.service.AUDIOBOOK"

        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "audii_channel_id"
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()

        val sessionActivityPendingIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent!!)
            .build()

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_PLAY) {
            val audiobook = intent.getParcelableExtra<Audiobook>(EXTRA_AUDIOBOOK)
            audiobook?.let {
                playAudiobook(it, it.currentPosition.first, it.currentPosition.second)
            }
        }

        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @OptIn(UnstableApi::class)
    private fun playAudiobook(audiobook: Audiobook, chapter: Int, position: Long) {
        val doc = DocumentFile.fromSingleUri(this, audiobook.uriString.toUri())
        if (doc?.isDirectory == true) {
            val mediaItems = getMediaUris(doc)
            mediaSession?.player?.setMediaItems(mediaItems)
        } else {
            mediaSession?.player?.setMediaItem(MediaItem.fromUri(audiobook.uriString.toUri()))
        }

        mediaSession?.player?.apply {
            prepare()
            seekTo(chapter, position)
            play()
        }
    }

    private fun getMediaUris(file: DocumentFile): List<MediaItem> {
        return file.listFiles()
            .filter { it.type?.startsWith("audio/") == true }
            .sortedBy { it.name }
            .map { MediaItem.fromUri(it.uri) }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player?.playWhenReady == false || player?.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
package com.acube.audii.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.acube.audii.R
import com.acube.audii.model.database.Audiobook
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerService : MediaSessionService() {

    var mediaSession: MediaSession? = null
    private val binder = PlayerBinder()

    inner class PlayerBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return binder
    }

    companion object {
        const val ACTION_PLAY = "com.acube.audii.service.PLAY"
        const val EXTRA_AUDIOBOOK = "com.acube.audii.service.AUDIOBOOK"
        const val NEXT_CHAPTER = "com.acube.audii.service.NEXT_CHAPTER"
        const val PREVIOUS_CHAPTER = "com.acube.audii.service.PREVIOUS_CHAPTER"
        const val PAUSE = "com.acube.audii.service.PAUSE"
        const val STOP = "com.acube.audii.service.STOP"
        const val CONTINUE = "com.acube.audii.service.CONTINUE"

        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "audii_channel_id"
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setNotificationId(NOTIFICATION_ID)
            .setChannelId(CHANNEL_ID)
            .build()

        setMediaNotificationProvider(notificationProvider)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val audiobook = intent.getParcelableExtra<Audiobook>(EXTRA_AUDIOBOOK)
                audiobook?.let {
                    playAudiobook(it, it.currentPosition.first, it.currentPosition.second)
                }
            }
            NEXT_CHAPTER -> mediaSession?.player?.seekToNextMediaItem()
            PREVIOUS_CHAPTER -> {
                if ((mediaSession?.player?.currentMediaItemIndex ?: 0) > 0) {
                    mediaSession?.player?.seekToPreviousMediaItem()
                }
            }
            PAUSE -> mediaSession?.player?.pause()
            STOP -> stopAudioBook()
            CONTINUE -> mediaSession?.player?.play()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_description)
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun playAudiobook(audiobook: Audiobook, chapter: Int, position: Long) {
        val doc = DocumentFile.fromSingleUri(this,audiobook.uriString.toUri())
        if (doc?.isDirectory == true) {
            mediaSession?.player?.addMediaItems(getMediaUris(doc))
        }
        mediaSession?.player?.apply {
            prepare()
            seekTo(chapter, position)
            play()
        }
    }

    fun getMediaUris(file: DocumentFile): List<MediaItem> {
        val mediaItems = mutableListOf<MediaItem>()
        file.listFiles().filter { it.type?.startsWith("audio/") == true }
            .sortedBy { it.name }
            .forEach {
                mediaItems.add(MediaItem.fromUri(it.uri))
            }
        return mediaItems
    }

    fun stopAudioBook() {
        mediaSession?.player?.apply {
            stop()
            clearMediaItems()
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

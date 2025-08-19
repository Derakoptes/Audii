package com.acube.audii.repository.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.provider.DocumentsContract
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.acube.audii.model.database.Audiobook
import com.acube.audii.service.PlayerService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class Chapter(
    val title: String,
    val duration: Long,
)
@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>
    private var mediaController: MediaController? = null
    private val retriever by lazy { MediaMetadataRetriever() }

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _currentAudiobook = MutableStateFlow<Audiobook?>(null)
    val currentAudiobook: StateFlow<Audiobook?> = _currentAudiobook.asStateFlow()

    private val _currentPosition: MutableStateFlow<Long> = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _retrievedChapters: MutableStateFlow<List<Chapter>> = MutableStateFlow(emptyList())
    val retrievedChapters = _retrievedChapters.asStateFlow()

    private val _currentDuration: MutableStateFlow<Long> = MutableStateFlow(0L)
    val currentDuration = _currentDuration.asStateFlow()

    private val _currentChapter: MutableStateFlow<Int> = MutableStateFlow(0)
    val currentChapter = _currentChapter.asStateFlow()

    private val _totalChapters: MutableStateFlow<Int> = MutableStateFlow(0)
    val totalChapters = _totalChapters.asStateFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isPlaying: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _playbackSpeed: MutableStateFlow<Float> = MutableStateFlow(1.0f)
    val playbackSpeed = _playbackSpeed.asStateFlow()

    init {
        setUpController()
    }


    private fun setUpController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlayerService::class.java))

        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        mediaControllerFuture.addListener({
            mediaController = mediaControllerFuture.get()
            mediaController?.addListener(playerListener)

            startTracking()
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentChapter.value = mediaController?.currentMediaItemIndex ?: 0
            _totalChapters.value = mediaController?.mediaItemCount ?: 0
            _currentDuration.value =
                _retrievedChapters.value.map{it.duration}.getOrNull(_currentChapter.value) ?: 0L
            /*Using this instead of the mediaController duration
         due to getting weird values from it probable due to timing.
         Will also need chapters later though so its fine
            */
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _isLoading.value = playbackState == Player.STATE_BUFFERING
            _isPlaying.value = mediaController?.isPlaying == true
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
    }

    fun playAudiobook(audiobook: Audiobook) {
        setFallBackDuration()//shouldnt get here if audiobook doesnt exist
        _currentAudiobook.value = audiobook
        val intent = Intent(context, PlayerService::class.java).apply {
            action = PlayerService.ACTION_PLAY
            putExtra(PlayerService.EXTRA_AUDIOBOOK, audiobook)
        }
        ContextCompat.startForegroundService(context, intent)
        _playbackSpeed.value = audiobook.speed
    }

    fun nextChapter(){
        mediaController?.seekTo(mediaController?.nextMediaItemIndex ?:0,0L)
    }
    fun previousChapter(){
        if(mediaController?.currentMediaItemIndex!=0) {
            mediaController?.seekTo((mediaController?.currentMediaItemIndex ?:1 )-1, 0L)
        }
    }
    fun skipForward(){
        val skipAmount = (_currentAudiobook.value?.skipTimings?.first ?: 10) * 1000
        val newPosition = _currentPosition.value + skipAmount
        if (newPosition < _currentDuration.value) {
            mediaController?.seekTo(newPosition)
        } else {
            nextChapter()
        }
    }
    fun skipBackward(){
        val skipAmount = (_currentAudiobook.value?.skipTimings?.first ?: 10) * 1000
        val newPosition = _currentPosition.value - skipAmount
        mediaController?.seekTo(
            if (newPosition < 0) 0L else newPosition
        )
    }
    fun changeSpeed(speed:Float){
        if(!(speed<0.5f || speed >3f) ){
            mediaController?.setPlaybackSpeed(speed)
            _playbackSpeed.value=speed
        }
    }
    fun stopPlaying(){
        mediaController?.stop()
        mediaController?.clearMediaItems()
        _currentAudiobook.value = null
    }
    fun playPause() {
        if(_isPlaying.value) {
            mediaController?.pause()
        }else{
            mediaController?.play()
        }
    }
    fun seekTo(time:Long){
        if(_currentDuration.value>=time) {
            mediaController?.seekTo(
                time
            )
        }
    }
    fun goToChapter(chapter: Int){
        if((mediaController?.mediaItemCount ?: 0) > chapter){
            mediaController?.seekTo(chapter,0L)
        }
    }
    private fun startTracking() {
        mediaController?.let {
            scope.launch {
                while (true) {
                    _currentPosition.value = it.currentPosition
                    delay(1000)
                }
            }
        }
    }

    private fun setFallBackDuration() {
        scope.launch {
            val doc = if (DocumentsContract.isTreeUri(_currentAudiobook.value?.uriString?.toUri())){
                DocumentFile.fromTreeUri(context,_currentAudiobook.value?.uriString?.toUri()!!)
            }else{
                DocumentFile.fromSingleUri(context,_currentAudiobook.value?.uriString?.toUri()!!)
            }
            doc
                ?.let { it ->
                    if (it.isDirectory) {
                        it.listFiles()
                            .filter { it.type?.startsWith("audio/") == true }
                            .sortedBy { it.name }
                            .forEach {
                                retriever.setDataSource(context, it.uri)

                                _retrievedChapters.value += Chapter(
                                    title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Unknown",
                                    duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                        ?.toLong() ?: 0L
                                )
                            }
                    } else {
                        retriever.setDataSource(context, it.uri)
                        _retrievedChapters.value +=  Chapter(
                            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Unknown",
                            duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                ?.toLong() ?: 0L
                        )
                    }
                }
        }
    }

    fun release() {
        _currentAudiobook.value = null
        mediaController?.removeListener(playerListener)
        MediaController.releaseFuture(mediaControllerFuture)
        mediaController = null
        scope.cancel()
    }
}
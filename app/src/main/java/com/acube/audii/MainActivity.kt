package com.acube.audii

import AudiobookListScreen
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels

import com.acube.audii.model.database.Audiobook
import com.acube.audii.ui.theme.AudiiTheme
import com.acube.audii.viewModel.AudiobookViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.Flow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val audiobookViewModel : AudiobookViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val audiobooks: Flow<List<Audiobook>> = audiobookViewModel.audioBookUiState.value.audiobooks

        setContent {
            AudiiTheme {
                AudiobookListScreen(
                    audiobooks = audiobooks,
                )
            }
        }
    }
}

@HiltAndroidApp
class MyApplication : Application() {}
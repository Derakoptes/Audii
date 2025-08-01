package com.acube.audii

import android.content.Context
import androidx.room.Room
import com.acube.audii.model.database.AudiobookDao
import com.acube.audii.model.database.AudiobookDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule{
    @Singleton
    @Provides
    fun provideAudiobookDatabase(@ApplicationContext context: Context) : AudiobookDatabase{
        return Room.databaseBuilder(context, AudiobookDatabase::class.java,"audiobooks").build()
    }

    @Singleton
    @Provides
    fun provideAudiobookDao(database: AudiobookDatabase)=database.audiobookDao()
}
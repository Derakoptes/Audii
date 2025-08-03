package com.acube.audii.modules

import com.acube.audii.model.repository.AudiobookRepository
import com.acube.audii.model.repository.AudiobookRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAudiobookRepository(
        audiobookRepositoryImpl: AudiobookRepositoryImpl
    ): AudiobookRepository

}
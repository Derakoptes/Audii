package com.acube.audii.modules

import com.acube.audii.model.parser.AudiobookParser
import com.acube.audii.model.parser.MapAudiobook
import com.acube.audii.model.repository.AudiobookRepository
import com.acube.audii.model.repository.AudiobookRepositoryImpl
import com.acube.audii.repository.audioBook.DatasourceRepositoryImpl
import com.acube.audii.repository.audioBook.DatasourceRepository
import com.acube.audii.repository.parser.AudiobookParserImpl
import com.acube.audii.repository.parser.MapAudiobookImpl
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

    @Binds
    abstract fun bindAudiobookParser(
        audiobookParserImpl: AudiobookParserImpl
    ): AudiobookParser

    @Binds
    abstract fun bindMapAudiobook(
        mapAudiobookImpl: MapAudiobookImpl
    ): MapAudiobook

    @Binds
    abstract fun bindDataSourceRepository(
        dataSourceRepositoryImpl: DatasourceRepositoryImpl
    ): DatasourceRepository
}
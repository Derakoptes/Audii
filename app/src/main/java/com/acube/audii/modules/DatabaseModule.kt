package com.acube.audii.modules

import android.content.Context
import androidx.room.Room
import com.acube.audii.model.database.AudiobookDatabase
import com.acube.audii.model.database.DatasourceDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule{
    @Provides
    @Singleton
    fun provideAudiobookDatabase(@ApplicationContext context: Context) : AudiobookDatabase {
        return Room.databaseBuilder(context, AudiobookDatabase::class.java,"audiobooks").build()
    }

    @Provides
    @Singleton
    fun provideDatasourceDatabase(@ApplicationContext context: Context) : DatasourceDatabase {
        return Room.databaseBuilder(context, DatasourceDatabase::class.java,"datasource").build()
    }

    @Provides
    @Singleton
    fun provideAudiobookDao(database: AudiobookDatabase)=database.audiobookDao()

    @Provides
    @Singleton
    fun provideDatasourceDao(database: DatasourceDatabase)=database.datasourceDao()
}
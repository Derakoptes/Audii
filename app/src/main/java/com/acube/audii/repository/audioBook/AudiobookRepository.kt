package com.acube.audii.model.repository

import com.acube.audii.model.database.Audiobook
import com.acube.audii.model.database.AudiobookDao
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

interface AudiobookRepository {
    fun getAllAudiobooks(): Flow<List<Audiobook>>
    suspend fun getAudiobooks(): List<Audiobook>
    suspend fun getAudiobookById(id: String): Audiobook?
    suspend fun addAudiobook(audiobook: Audiobook)
    suspend fun updateAudiobook(audiobook: Audiobook)
    suspend fun deleteAudiobook(id: String)
    suspend fun updatePlaybackPosition(id: String, position: Pair<Int,Long>)
    suspend fun updatePlaybackSpeed(id: String, speed: Float)
    suspend fun updateAudiobookCollection(id:String,collections:List<Int>)
}

class AudiobookRepositoryImpl @Inject constructor(
    private val audiobookDao: AudiobookDao
): AudiobookRepository{
    override fun getAllAudiobooks(): Flow<List<Audiobook>> {
        return audiobookDao.getAllAudiobooks()
    }

    override suspend fun getAudiobooks(): List<Audiobook> {
        return audiobookDao.getAudiobooks()
    }

    override suspend fun getAudiobookById(id: String): Audiobook? {
        return audiobookDao.getAudioBookById(id = id)
    }

    override suspend fun addAudiobook(audiobook: Audiobook) {
       return audiobookDao.insertAudiobook(audiobook=audiobook)
    }

    override suspend fun updateAudiobook(audiobook: Audiobook) {
        return audiobookDao.updateAudiobook(audiobook = audiobook)
    }

    override suspend fun deleteAudiobook(id: String) {
       return audiobookDao.deleteAudiobook(id=id)
    }

    override suspend fun updatePlaybackPosition(id: String, position: Pair<Int,Long>) {
      return audiobookDao.updatePlaybackPosition(id = id, currentPosition =position)
    }

    override suspend fun updatePlaybackSpeed(id: String, speed: Float) {
        return audiobookDao.updatePlaybackSpeed(id = id, speed = speed)
    }

    override suspend fun updateAudiobookCollection(
        id: String,
        collections: List<Int>
    ) {
        audiobookDao.updateAudiobookCollection(id = id, collections = collections)
    }

}


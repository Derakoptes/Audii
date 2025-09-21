package com.acube.audii.repository.audioBook

import com.acube.audii.model.database.CollectionDao
import com.acube.audii.model.database.Collection
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


interface CollectionRepository{
    fun getAllCollections(): Flow<List<Collection>>
    suspend fun addCollection(collection: Collection)
    suspend fun deleteCollection(collection: Collection)
}

class CollectionRepositoryImpl @Inject constructor(
    private val collectionDao: CollectionDao
): CollectionRepository{
    override fun getAllCollections(): Flow<List<Collection>> {
       return collectionDao.getCollections()
    }

    override suspend fun addCollection(collection: Collection) {
       return collectionDao.addCollection(collection)
    }

    override suspend fun deleteCollection(collection: Collection) {
        return collectionDao.deleteCollection(collection)
    }

}


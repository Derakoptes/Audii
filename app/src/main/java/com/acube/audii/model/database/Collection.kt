package com.acube.audii.model.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)

@Dao
interface CollectionDao{
    @Query("SELECT * FROM collections ORDER BY name ASC")
    fun getCollections(): Flow<List<Collection>>
    @Insert
    suspend fun addCollection(collection: Collection)
    @Delete
    suspend fun deleteCollection(collection: Collection)
}

@Database(entities = [Collection::class], exportSchema = true, version = 1)
abstract class CollectionDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
}





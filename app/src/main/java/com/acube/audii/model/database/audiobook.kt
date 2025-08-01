package com.acube.audii.model.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "audiobooks")
data class Audiobook(
    @PrimaryKey val id: String,
    val title:String,
    val author:String,
    val filePath:String,
    val duration: Long, //in millis
    val currentPosition: Pair<Int,Long> = Pair(0,0),//chapter and progress in chapter
    val isCompleted: Boolean=false,
    val coverImageUrl:String? = null,
    val modifiedDate : Long = System.currentTimeMillis()//time since epoch
)

@Dao
interface AudiobookDao{
    @Query("SELECT * FROM audiobooks ORDER BY modifiedDate DESC")
    fun getAllAudiobooks(): Flow<List<Audiobook>>

    @Query("SELECT * FROM audiobooks WHERE id = :id")
    suspend fun getAudioBookById(id:String): Audiobook?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAudiobook(audiobook: Audiobook)

    @Update
    suspend fun updateAudiobook(audiobook: Audiobook)

    @Delete
    suspend fun deleteAudiobook(id: String)

    @Query("UPDATE audiobooks SET currentPosition = :currentPosition WHERE id = :id")
    suspend fun updatePlaybackPosition(id: String, currentPosition: Pair<Int,Long>)
}

@Database(entities = [Audiobook::class], version = 1)
abstract class AudiobookDatabase : RoomDatabase(){
    abstract fun audiobookDao(): AudiobookDao
}








package com.acube.audii.model.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "audiobooks")
data class Audiobook(
    @PrimaryKey val id: String,
    val title:String,
    val author:String,
    val narrator:String,
    val uriString:String,
    val duration: List<Long>, //in millis
    val currentPosition: Pair<Int,Long> = Pair(0,0),//chapter and progress in chapter
    val coverImageUriPath:String? = null,
    val modifiedDate : Long = System.currentTimeMillis()//time since epoch
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createLongArray()!!.toList(),
        Pair(parcel.readInt(), parcel.readLong()),
        parcel.readString(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(author)
        parcel.writeString(narrator)
        parcel.writeString(uriString)
        parcel.writeLongArray(duration.toLongArray())
        parcel.writeInt(currentPosition.first)
        parcel.writeLong(currentPosition.second)
        parcel.writeString(coverImageUriPath)
        parcel.writeLong(modifiedDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Audiobook> {
        override fun createFromParcel(parcel: Parcel): Audiobook {
            return Audiobook(parcel)
        }

        override fun newArray(size: Int): Array<Audiobook?> {
            return arrayOfNulls(size)
        }
    }
}

class Converters{
    @TypeConverter
    fun listOfLongToString(list: List<Long>):String{
        return list.joinToString(",")
    }
    @TypeConverter
    fun stringToListOfLong(string: String):List<Long>{
        return string.split(",").map { it.toLong() }
    }

    @TypeConverter
    fun pairOfIntLongToString(pair: Pair<Int,Long>):String{
        return "${pair.first},${pair.second}"
    }
    @TypeConverter
    fun stringToPairOfIntLong(string: String):Pair<Int,Long>{
        val (first,second) = string.split(",")
        return Pair(first.toInt(),second.toLong())
    }
}
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

    @Query("DELETE FROM audiobooks WHERE id = :id")
    suspend fun deleteAudiobook(id: String)

    @Query("UPDATE audiobooks SET currentPosition = :currentPosition WHERE id = :id")
    suspend fun updatePlaybackPosition(id: String, currentPosition: Pair<Int,Long>)
}

//TODO:look into exporting the schema
@Database(entities = [Audiobook::class], exportSchema = false,version = 1)
@TypeConverters(Converters::class)
abstract class AudiobookDatabase : RoomDatabase(){
    abstract fun audiobookDao(): AudiobookDao
}








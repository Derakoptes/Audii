package com.acube.audii.model.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "datasource")
data class Datasource(
    @PrimaryKey val id:String,
    val uri:String,
)
@Dao
interface DatasourceDao{
    @Query("SELECT * FROM datasource")
    suspend fun getAllDatasources(): List<Datasource>
    @Query("SELECT * FROM datasource WHERE id = :id")
    suspend fun getDatasourceById(id:String): Datasource?
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDatasource(datasource: Datasource)

    @Query("DELETE FROM datasource WHERE id = :id")
    suspend fun deleteDatasource(id:String)
}

@Database(entities = [Datasource::class], exportSchema = true,version = 1)
abstract class DatasourceDatabase : RoomDatabase(){
    abstract fun datasourceDao(): DatasourceDao
}








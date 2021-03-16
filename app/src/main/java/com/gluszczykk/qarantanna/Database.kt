package com.gluszczykk.qarantanna

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Database(entities = [Config::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configDao(): ConfigDao
}

@Dao
interface ConfigDao {

    @Query("SELECT * FROM config ORDER BY config.id LIMIT 1")
    fun get(): Flow<Config>

    @Insert
    fun insert(config: Config)
}
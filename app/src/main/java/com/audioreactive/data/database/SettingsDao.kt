package com.audioreactive.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.audioreactive.data.Settings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert
    suspend fun createSettings(settings: Settings)

    @Query("SELECT * FROM settings LIMIT 1")
    fun getSettingsFlow(): Flow<Settings?>

    @Query("SELECT * FROM settings LIMIT 1")
    fun getSettings(): Settings?

    @Update
    suspend fun updateSettings(settings: Settings)
}
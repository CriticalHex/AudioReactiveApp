package com.audioreactive.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.audioreactive.data.AudioReactiveRepo
import com.audioreactive.data.Settings

@Database(entities = [Settings::class], version = 1)
@TypeConverters(AudioReactiveTypeConverters::class)
abstract class AudioReactiveDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: AudioReactiveDatabase? = null
        fun getInstance(context: Context): AudioReactiveDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context, AudioReactiveDatabase::class.java, "audioreactive-database"
                ).build().also { INSTANCE = it }
            }
        }
    }

    abstract val settingsDao: SettingsDao
}
package com.audioreactive.data

import android.content.Context
import com.audioreactive.data.database.AudioReactiveDatabase
import com.audioreactive.data.database.SettingsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class AudioReactiveRepo private constructor(private val settingsDao: SettingsDao) {
    companion object {
        private const val LOG_TAG = "AR.AudioReactiveRepo"
        private var INSTANCE: AudioReactiveRepo? = null

        fun getInstance(context: Context) = INSTANCE ?: synchronized(this) {
            AudioReactiveRepo(
                AudioReactiveDatabase.getInstance(context).settingsDao
            ).also { INSTANCE = it }
        }
    }

    private val _serviceRunning = MutableStateFlow(false)
    val serviceRunning: StateFlow<Boolean> = _serviceRunning.asStateFlow()

    fun updateServiceRunning(running: Boolean) {
        _serviceRunning.value = running
    }

    fun getSettingsFlow(): Flow<Settings> = settingsDao.getSettingsFlow().onStart {
        if (settingsDao.getSettings() == null) {
            settingsDao.createSettings(Settings())
        }
    }.map {
        it ?: Settings()
    }.distinctUntilChanged()

    fun getSettings(): Settings = settingsDao.getSettings() ?: Settings()

    suspend fun updateSettings(settings: Settings) = settingsDao.updateSettings(settings)

}

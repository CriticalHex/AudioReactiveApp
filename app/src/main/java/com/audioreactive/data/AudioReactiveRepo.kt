package com.audioreactive.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioReactiveRepo private constructor(context: Context) {
    companion object {
        private const val LOG_TAG = "AR.AudioReactiveRepo"
        private var INSTANCE: AudioReactiveRepo? = null

        fun getInstance(context: Context) =
            INSTANCE ?: AudioReactiveRepo(context).also { INSTANCE = it }
    }
    private val _serviceRunning = MutableStateFlow(false)
    val serviceRunning: StateFlow<Boolean> = _serviceRunning.asStateFlow()

    fun updateServiceRunning(running: Boolean) {
        _serviceRunning.value = running
    }

}

package com.audioreactive.ui.viewmodel

import androidx.lifecycle.ViewModel

class LatticeViewModel : ViewModel() {
    private var startNanos: Long? = null
    private var accumulatedSeconds: Double = 0.0

    var timeScale: Double = 0.1

    fun timeSeconds(nowNanos: Long): Double {
        val s = startNanos
        return if (s == null) {
            startNanos = nowNanos
            accumulatedSeconds
        } else {
            accumulatedSeconds + ((nowNanos - s) / 1_000_000_000.0) * timeScale
        }
    }

    fun pause(nowNanos: Long) {
        val s = startNanos ?: return
        accumulatedSeconds += ((nowNanos - s) / 1_000_000_000.0) * timeScale
        startNanos = null
    }

    fun reset() {
        startNanos = null
        accumulatedSeconds = 0.0
    }
}
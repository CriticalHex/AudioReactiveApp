package com.audioreactive.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class RotationSensorManager(context: Context) {

    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private var listener: SensorEventListener? = null

    fun start(onRotation: (FloatArray) -> Unit) {
        if (rotationSensor == null) return
        val matrix = FloatArray(9)
        val reference = FloatArray(9)
        val relative = FloatArray(9)
        var hasReference = false

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(matrix, event.values)
                if (!hasReference) {
                    matrix.copyInto(reference)
                    hasReference = true
                }
                multiplyByTranspose(matrix, reference, relative)
                onRotation(relative)
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        listener?.let { sensorManager.unregisterListener(it) }
        listener = null
    }

    private fun multiplyByTranspose(A: FloatArray, B: FloatArray, result: FloatArray) {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                var sum = 0f
                for (k in 0 until 3) sum += A[i * 3 + k] * B[j * 3 + k]
                result[i * 3 + j] = sum
            }
        }
    }
}

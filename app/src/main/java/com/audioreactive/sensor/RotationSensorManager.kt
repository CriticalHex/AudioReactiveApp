package com.audioreactive.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class RotationSensorManager(context: Context) {

    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private var listener: SensorEventListener? = null

    fun start(onRotation: (FloatArray) -> Unit) {
        if (rotationSensor == null) return

        val matrix = FloatArray(9)
        val reference = FloatArray(9)
        val relative = FloatArray(9)
        val remapped = FloatArray(9)
        var hasReference = false

        val driftAlpha = 0.007f

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(matrix, event.values)
                if (!hasReference) {
                    matrix.copyInto(reference)
                    hasReference = true
                } else {
                    for (i in 0 until 9) {
                        reference[i] = reference[i] * (1f - driftAlpha) + matrix[i] * driftAlpha
                    }
                    renormalize(reference)
                }
                multiplyByTranspose(matrix, reference, relative)
                applyAxisRemap(relative, remapped)
                onRotation(remapped)
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        listener?.let { sensorManager.unregisterListener(it) }
        listener = null
    }

    private fun multiplyByTranspose(matrixA: FloatArray, matrixB: FloatArray, result: FloatArray) {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                var sum = 0f
                for (k in 0 until 3) sum += matrixA[i * 3 + k] * matrixB[j * 3 + k]
                result[i * 3 + j] = sum
            }
        }
    }

    private fun applyAxisRemap(r: FloatArray, out: FloatArray) {
        val a = r[0]; val b = r[1]; val c = r[2]
        val d = r[3]; val e = r[4]; val f = r[5]
        val g = r[6]; val h = r[7]; val i = r[8]
        out[0] =  a; out[1] = -c; out[2] = -b
        out[3] = -g; out[4] =  i; out[5] =  h
        out[6] = -d; out[7] =  f; out[8] =  e
    }

    private fun renormalize(r: FloatArray) {
        var len = sqrt((r[0] * r[0] + r[1] * r[1] + r[2] * r[2]).toDouble()).toFloat()
        if (len > 0f) {
            r[0] /= len; r[1] /= len; r[2] /= len
        }
        val dot = r[3] * r[0] + r[4] * r[1] + r[5] * r[2]
        r[3] -= dot * r[0]; r[4] -= dot * r[1]; r[5] -= dot * r[2]
        len = sqrt((r[3] * r[3] + r[4] * r[4] + r[5] * r[5]).toDouble()).toFloat()
        if (len > 0f) {
            r[3] /= len; r[4] /= len; r[5] /= len
        }
        r[6] = r[1] * r[5] - r[2] * r[4]
        r[7] = r[2] * r[3] - r[0] * r[5]
        r[8] = r[0] * r[4] - r[1] * r[3]
    }
}

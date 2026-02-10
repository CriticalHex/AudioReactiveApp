package com.audioreactive

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.audioreactive.service.AudioCaptureService
import com.audioreactive.ui.components.StartAudioCaptureButton
import com.audioreactive.ui.screens.VisualizerScreen
import com.audioreactive.ui.theme.AudioReactiveTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var audioService: AudioCaptureService? = null
    private val spectrumState = mutableStateOf(FloatArray(0))

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            audioService = (binder as AudioCaptureService.LocalBinder).getService()
            println("Service bound via connection")
            audioService?.registerListener(serviceListener)
            observeSpectrum()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            println("Service disconnected via connection")
            audioService?.unregisterListener(serviceListener)
            audioService = null
        }
    }

    private val projectionLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                startAudioService(result.resultCode, result.data!!)
            }
        }

    //had to specify type here or else recursive type error??
    private val serviceListener: AudioCaptureService.ServiceEventListener  = object : AudioCaptureService.ServiceEventListener {
        override fun onCaptureStopped() {
            runOnUiThread {
                println("Capture stopped, unbinding")
                unbindService(serviceConnection)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AudioReactiveTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StartAudioCaptureButton(Modifier.padding(innerPadding)) {
                        requestScreenCaptureAndStartService()
                    }
                    VisualizerScreen(spectrumState, Modifier.padding(innerPadding))
                }
            }
        }

        bindService(
            Intent(this, AudioCaptureService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    private fun requestScreenCaptureAndStartService() {
        val mgr = getSystemService(MediaProjectionManager::class.java)
        projectionLauncher.launch(mgr.createScreenCaptureIntent())
    }

    private fun startAudioService(resultCode: Int, data: Intent) {
        val intent = Intent(this, AudioCaptureService::class.java).apply {
            putExtra(AudioCaptureService.EXTRA_RESULT_CODE, resultCode)
            putExtra(AudioCaptureService.EXTRA_DATA, data)
        }
        println("Starting foreground service")
        startForegroundService(intent)
        bindService(
            Intent(this, AudioCaptureService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    private fun observeSpectrum() {
        lifecycleScope.launch {
            audioService?.spectrumFlow()?.collect {
                spectrumState.value = it
            }
        }
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }

    override fun onPause() {
        println("Paused")
        super.onPause()
    }

    override fun onResume() {
        println("Resumed")
        super.onResume()
    }

    override fun onRestart() {
        println("Restarted")
        super.onRestart()
    }

    override fun onStop() {
        println("Stopped, isFinishing is $isFinishing")
        super.onStop()
    }
}
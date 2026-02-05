package com.audioreactive

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.audioreactive.ui.theme.AudioReactiveTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var audioService: AudioCaptureService? = null
    private val spectrumState = mutableStateOf(FloatArray(0))

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as AudioCaptureService.LocalBinder
            audioService = localBinder.getService()
            observeSpectrum()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
        }
    }

    private val projectionLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                startAudioService(result.resultCode, result.data!!)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AudioReactiveTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VisualizerScreen(spectrumState)
                }
            }
        }

        requestScreenCapture()
    }

    private fun requestScreenCapture() {
        val mgr = getSystemService(MediaProjectionManager::class.java)
        projectionLauncher.launch(mgr.createScreenCaptureIntent())
    }

    private fun startAudioService(resultCode: Int, data: Intent) {
        val intent = Intent(this, AudioCaptureService::class.java).apply {
            putExtra(AudioCaptureService.EXTRA_RESULT_CODE, resultCode)
            putExtra(AudioCaptureService.EXTRA_DATA, data)
        }

        startForegroundService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
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
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AudioReactiveTheme {
        Greeting("Android")
    }
}
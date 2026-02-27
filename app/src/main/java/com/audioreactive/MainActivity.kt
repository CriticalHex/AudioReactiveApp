package com.audioreactive

import com.audioreactive.ui.screens.VisualizerLattice
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.audioreactive.service.AudioCaptureService
import com.audioreactive.ui.components.ButtonWithText
import com.audioreactive.ui.components.SelectFileButton
import com.audioreactive.ui.components.StartAudioCaptureButton
import com.audioreactive.ui.screens.VisualizerLattice
import com.audioreactive.ui.screens.VisualizerScreen
import com.audioreactive.ui.theme.AudioReactiveTheme
import com.audioreactive.ui.viewmodel.AudioPlayerViewModel
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel.VisualizerIntent.UpdateSpectrumIntent
import com.audioreactive.ui.viewmodel.VisualizerViewModel.VisualizerIntent.UpdateVolumeIntent
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.sample
class MainActivity : ComponentActivity() {
    private var audioService: AudioCaptureService? = null
    private lateinit var audioPlayerViewModel: AudioPlayerViewModel
    private lateinit var visualizerViewModel: VisualizerViewModel

    private lateinit var latticeViewModel: LatticeViewModel

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

    val filePickerLauncher = registerForActivityResult(OpenDocument()) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            audioPlayerViewModel.loadAudio(it)
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
        audioPlayerViewModel = ViewModelProvider(this)[AudioPlayerViewModel::class.java]
        visualizerViewModel = ViewModelProvider(this)[VisualizerViewModel::class.java]
        latticeViewModel = ViewModelProvider(this)[LatticeViewModel::class.java]
        setContent {
            AudioReactiveTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Black


                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        StartAudioCaptureButton {
                            requestScreenCaptureAndStartService()
                        }
                        SelectFileButton {filePickerLauncher.launch(arrayOf("audio/*"))}
                        ButtonWithText(
                            text = if (audioPlayerViewModel.player.isPlaying) "Pause" else "Play"
                        ) {
                            if (audioPlayerViewModel.player.isPlaying)
                                audioPlayerViewModel.pause()
                            else
                                audioPlayerViewModel.play()
                        }
                    }
                    VisualizerScreen(visualizerViewModel.state.value.spectrum, Modifier.padding(innerPadding))

                    Box(modifier = Modifier.padding(innerPadding)) {
                        VisualizerLattice(
                            modifier = Modifier.fillMaxSize(),
                            vm = latticeViewModel,
                            volume = visualizerViewModel.state.value.volume
                        )
                    }

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
            audioService?.volumeFlow()
                ?.sample(10)
                ?.collect { v ->
                    Log.d("VOLUME", "volume=${"%.3f".format(v)}")
                    visualizerViewModel.handleIntent(UpdateVolumeIntent(v))
                }

        }

        lifecycleScope.launch {
            audioService?.spectrumFlow()?.collect { bands ->
                visualizerViewModel.handleIntent(UpdateSpectrumIntent(bands))
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
        audioPlayerViewModel.play()
        println("Resumed")
        super.onResume()
    }

    override fun onRestart() {
        println("Restarted")
        super.onRestart()
    }

    override fun onStop() {
        println("Stopped, isFinishing is $isFinishing")
        audioPlayerViewModel.pause()
        super.onStop()
    }
}
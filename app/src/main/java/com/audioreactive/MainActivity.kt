package com.audioreactive

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.audioreactive.service.AudioCaptureService
import com.audioreactive.ui.components.SelectFileButton
import com.audioreactive.ui.components.StartAudioCaptureButton
import com.audioreactive.ui.screens.SettingsScreen
import com.audioreactive.ui.screens.VisualizerLattice
import com.audioreactive.ui.screens.VisualizerScreen
import com.audioreactive.ui.theme.AudioReactiveTheme
import com.audioreactive.ui.viewmodel.AudioPlayerViewModel
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel.VisualizerIntent.UpdateSpectrumIntent
import com.audioreactive.ui.viewmodel.VisualizerViewModel.VisualizerIntent.UpdateVolumeIntent
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

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

    private val projectionLauncher = registerForActivityResult(StartActivityForResult()) { result ->
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

    private val serviceListener: AudioCaptureService.ServiceEventListener =
        object : AudioCaptureService.ServiceEventListener {
            override fun onCaptureStopped() {
                runOnUiThread {
                    println("Capture stopped, unbinding")
                    unbindService(serviceConnection)
                }
            }
        }

    private fun enableFullScreen() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioPlayerViewModel = ViewModelProvider(this)[AudioPlayerViewModel::class.java]
        visualizerViewModel = ViewModelProvider(this)[VisualizerViewModel::class.java]
        latticeViewModel = ViewModelProvider(this)[LatticeViewModel::class.java]

        enableFullScreen()

        setContent {
            AudioReactiveTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = true,
                    drawerContent = {
                        ModalDrawerSheet {
                            StartAudioCaptureButton {
                                requestScreenCaptureAndStartService()
                            }
                            SelectFileButton {
                                filePickerLauncher.launch(arrayOf("audio/*"))
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        drawerState.close()
                                    }
                                    navController.navigate("settings")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text("Settings")
                            }
                        }
                    }
                ) {
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                containerColor = Color.Black,
                                bottomBar = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .navigationBarsPadding()
                                            .padding(bottom = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MediaControlBar(audioPlayerViewModel = audioPlayerViewModel)
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    VisualizerScreen(visualizerViewModel.state.value.spectrum)
                                    VisualizerLattice(
                                        modifier = Modifier.fillMaxSize(),
                                        vm = latticeViewModel,
                                        volume = visualizerViewModel.state.value.volume
                                    )
                                }
                            }
                        }

                        composable("settings") {
                            SettingsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
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
            audioService?.volumeFlow()?.sample(1000)?.collect { v ->
//                Log.d("VOLUME", "volume=${"%.3f".format(v)}")
                visualizerViewModel.handleIntent(UpdateVolumeIntent(v))
            }
        }

        lifecycleScope.launch {
            audioService?.spectrumFlow()?.collect { bands ->
                Log.d("SPECTRUM", bands.joinToString {
                    "%.3f".format(it)
                })
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

@Composable
fun MediaControlBar(audioPlayerViewModel: AudioPlayerViewModel) {
    val isPlaying by audioPlayerViewModel.isPlaying

    Surface(
        modifier = Modifier.wrapContentWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { audioPlayerViewModel.playPrevious() }) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Song"
                )
            }

            IconButton(onClick = { audioPlayerViewModel.togglePlayback() }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause"
                )
            }

            IconButton(onClick = { audioPlayerViewModel.playNext() }) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Song"
                )
            }
        }
    }
}

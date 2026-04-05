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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
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
import com.audioreactive.ui.components.VisualizerLattice
import com.audioreactive.ui.components.VisualizerScreen
import com.audioreactive.ui.theme.AudioReactiveTheme
import com.audioreactive.ui.viewmodel.AudioPlayerViewModel
import com.audioreactive.ui.viewmodel.AudioReactiveViewModelFactory
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        private const val LOG_TAG: String = "AR.MainActivity"
    }
    private var audioService: AudioCaptureService? = null
    private lateinit var audioPlayerViewModel: AudioPlayerViewModel
    private lateinit var visualizerViewModel: VisualizerViewModel
    private lateinit var latticeViewModel: LatticeViewModel



    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            audioService = (binder as AudioCaptureService.LocalBinder).getService()
            Log.d(LOG_TAG, "Service bound via connection")
            observeSpectrum()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(LOG_TAG, "Service disconnected via connection")
            audioService = null
        }
    }

    private val projectionLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            startAudioService(result.data!!)
        }
    }

    val filePickerLauncher = registerForActivityResult(OpenDocument()) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            audioPlayerViewModel.dispatcher.invoke(AudioPlayerIntent.LoadAudio(it))
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
        audioPlayerViewModel = ViewModelProvider(
            store = this.viewModelStore,
            factory = AudioReactiveViewModelFactory(),
            defaultCreationExtras = AudioReactiveViewModelFactory.creationExtras(
                this.defaultViewModelCreationExtras,
                this
            )
        )[AudioPlayerViewModel::class]
        visualizerViewModel = ViewModelProvider(
            store = this.viewModelStore,
            factory = AudioReactiveViewModelFactory(),
            defaultCreationExtras = AudioReactiveViewModelFactory.creationExtras(
                this.defaultViewModelCreationExtras,
                this
            )
        )[VisualizerViewModel::class]
        latticeViewModel = ViewModelProvider(
            store = this.viewModelStore,
            factory = AudioReactiveViewModelFactory(),
            defaultCreationExtras = AudioReactiveViewModelFactory.creationExtras(
                this.defaultViewModelCreationExtras,
                this
            )
        )[LatticeViewModel::class]

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
                            val state = visualizerViewModel.stateFlow.collectAsState()
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

                                }
                                VisualizerScreen(state.value.spectrum)

                                VisualizerLattice(
                                    modifier = Modifier.fillMaxSize(),
                                    latticeViewModel = latticeViewModel,
                                    volume = state.value.volume
                                )
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

    private fun startAudioService(data: Intent) {
        val intent = Intent(this, AudioCaptureService::class.java).apply {
            putExtra(AudioCaptureService.EXTRA_DATA, data)
        }
        Log.d(LOG_TAG, "Starting foreground service")
        startForegroundService(intent)
        bindService(
            Intent(this, AudioCaptureService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    private fun observeSpectrum() {
        lifecycleScope.launch {
            audioService?.volumeFlow()?.sample(10)?.collect { volume ->
                visualizerViewModel.dispatcher.invoke(VisualizerIntent.UpdateVolume(volume))
            }
        }

        lifecycleScope.launch {
            audioService?.spectrumFlow()?.collect { spectrum ->
                visualizerViewModel.dispatcher.invoke(VisualizerIntent.UpdateSpectrum(spectrum))
            }
        }
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "Destroying activity")
        unbindService(serviceConnection)
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        Log.d(LOG_TAG, "Stopped, isFinishing is $isFinishing")
        audioPlayerViewModel.dispatcher.invoke(AudioPlayerIntent.Pause)
    }
}

@Composable
fun MediaControlBar(audioPlayerViewModel: AudioPlayerViewModel) {
    val state = audioPlayerViewModel.stateFlow.collectAsState()

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
            IconButton(onClick = { audioPlayerViewModel.dispatcher.invoke(AudioPlayerIntent.Previous) }) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Song"
                )
            }

            IconButton(onClick = { audioPlayerViewModel.dispatcher.invoke(AudioPlayerIntent.TogglePlayback) }) {
                Icon(
                    imageVector = if (state.value.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause"
                )
            }

            IconButton(onClick = { audioPlayerViewModel.dispatcher.invoke(AudioPlayerIntent.Next) }) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Song"
                )
            }
        }
    }
}

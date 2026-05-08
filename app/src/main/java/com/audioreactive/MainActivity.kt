package com.audioreactive

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import coil3.imageLoader
import coil3.key.Keyer
import coil3.memory.MemoryCache
import com.audioreactive.data.AudioReactiveRepo
import com.audioreactive.player.AudioPlayer
import com.audioreactive.service.AudioCaptureService
import com.audioreactive.ui.navigation.AudioReactiveNavHost
import com.audioreactive.ui.navigation.SnackbarManager
import com.audioreactive.ui.theme.AudioReactiveTheme
import com.audioreactive.ui.viewmodel.AudioPlayerViewModel
import com.audioreactive.ui.viewmodel.AudioReactiveViewModelFactory
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent
import com.audioreactive.ui.viewmodel.intent.QueuedAudio
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import java.io.File

@UnstableApi
class MainActivity : ComponentActivity() {
    companion object {
        private const val LOG_TAG: String = "AR.MainActivity"
    }

    private var audioService: AudioCaptureService? = null
    private lateinit var audioPlayerViewModel: AudioPlayerViewModel
    private lateinit var visualizerViewModel: VisualizerViewModel
    private lateinit var latticeViewModel: LatticeViewModel
    private var appendToQueue = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            audioService = (binder as AudioCaptureService.LocalBinder).getService()
            Log.d(LOG_TAG, "Service bound via connection")
            audioService?.connectToAudioPlayer(AudioPlayer.getInstance(this@MainActivity))
            audioService?.connectToRepo(AudioReactiveRepo.getInstance(this@MainActivity))
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

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
        if (uris.isEmpty()) return@registerForActivityResult

        val songs = uris.map { uri ->
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            QueuedAudio(
                uri = uri,
                title = getFileNameWithoutExtension(uri)
            )
        }

        if (appendToQueue) {
            songs.forEach { song ->
                audioPlayerViewModel.dispatcher.invoke(
                    AudioPlayerIntent.QueueAudio(song)
                )
            }
        } else {
            audioPlayerViewModel.dispatcher.invoke(
                AudioPlayerIntent.SetQueue(songs)
            )
        }
    }

    val photoPickerLauncher = registerForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { fileUri ->
            val inputStream = contentResolver.openInputStream(fileUri)
            val file = File(filesDir, VisualizerViewModel.CUSTOM_IMAGE_NAME)

            // CRAZY thing to get AsyncImage to see the same file as new
            // the debugging I did to find out what it uses as a key was wild
            imageLoader.memoryCache?.remove(MemoryCache.Key(file.toURI().toString()))

            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                    visualizerViewModel.dispatcher.invoke(VisualizerIntent.SetBackgroundImage(true))
                }
            }

        }
    }

    fun launchAudioCaptureRequest() {
        requestScreenCaptureAndStartService()
    }

    fun stopAudioCapture() {
        audioService?.stopCapture()
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
        )[AudioPlayerViewModel::class.java]

        visualizerViewModel = ViewModelProvider(
            store = this.viewModelStore,
            factory = AudioReactiveViewModelFactory(),
            defaultCreationExtras = AudioReactiveViewModelFactory.creationExtras(
                this.defaultViewModelCreationExtras,
                this
            )
        )[VisualizerViewModel::class.java]

        latticeViewModel = ViewModelProvider(
            store = this.viewModelStore,
            factory = AudioReactiveViewModelFactory(),
            defaultCreationExtras = AudioReactiveViewModelFactory.creationExtras(
                this.defaultViewModelCreationExtras,
                this
            )
        )[LatticeViewModel::class.java]

        enableFullScreen()

        setContent {
            AudioReactiveTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    SnackbarManager.messages.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    AudioReactiveNavHost(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController
                    )

                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
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
        if (isFinishing) audioPlayerViewModel.dispatcher.invoke(AudioPlayerIntent.Pause)
    }

    fun launchAudioFilePicker() {
        appendToQueue = false
        filePickerLauncher.launch(arrayOf("audio/*"))
    }

    fun launchQueueFilePicker() {
        appendToQueue = true
        filePickerLauncher.launch(arrayOf("audio/*"))
    }

    private fun getFileNameWithoutExtension(uri: Uri): String {
        val displayName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        } ?: uri.lastPathSegment ?: "Unknown Song"

        return displayName.substringBeforeLast(".")
    }
}

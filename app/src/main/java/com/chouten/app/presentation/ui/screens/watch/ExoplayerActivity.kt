package com.chouten.app.presentation.ui.screens.watch

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Rational
import android.util.TypedValue
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C.SELECTION_FLAG_DEFAULT
import androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import com.chouten.app.R
import com.chouten.app.common.UiText
import com.chouten.app.domain.model.ModuleModel
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.domain.proto.moduleDatastore
import com.chouten.app.domain.use_case.module_use_cases.ModuleUseCases
import com.chouten.app.presentation.ui.components.snackbar.SnackbarHost
import com.chouten.app.presentation.ui.screens.info.InfoResult
import com.chouten.app.presentation.ui.theme.ChoutenTheme
import com.lagradost.nicehttp.Requests
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Exoplayer Activity used for playing media defined in a [WatchBundle].
 * @see [WatchBundle]
 */
@AndroidEntryPoint
class ExoplayerActivity : ComponentActivity() {

    /**
     * Used for detecting the currently selected Module
     */
    @Inject
    lateinit var moduleUseCases: ModuleUseCases

    /**
     * The handler is used to recursively check & update the playback position
     */
    private var handler = Handler(Looper.getMainLooper())

    /**
     * The aspect ratio of the video, defaulting to 16:9 (16/9)
     */
    private var aspectRatio = Rational(16, 9)
    private lateinit var exoplayer: ExoPlayer

    /**
     * Used to construct the [DataSource] for the [ExoPlayer].
     * Allows for custom headers to be set and used when requesting the media.
     */
    private lateinit var dataSourceFactory: DataSource.Factory

    /**
     * The [MediaItem] to be played by the [ExoPlayer]
     */
    private lateinit var mediaItem: MediaItem

    /**
     * Is the ExoPlayer initialized? Used to prevent the player from being initialized multiple times
     */
    private var isInitialized = false

    private lateinit var media: List<InfoResult.MediaListItem>
    private lateinit var watchBundle: WatchBundle
    private lateinit var sources: WatchResult
    private lateinit var servers: List<WatchResult.ServerData>

    private lateinit var mimeType: String

    /**
     * HTTP client with a 5MiB cache and a default cache time of 30 minutes
     */
    private val client by lazy {
        val context = application.applicationContext
        val cache = Cache(
            File(
                context.cacheDir, "http_cache"
            ), 1024L * 1024L * 5L // 5 MiB (not MB)
        )

        val okHttpClient = OkHttpClient.Builder().apply {
            followRedirects(true)
            followSslRedirects(true)
            cache(cache)
        }.build()

        val defaultHeaders = mapOf(
            "User-Agent" to "Mozilla/5.0 (Linux; Android %s; %s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Mobile Safari/537.36".format(
                Build.VERSION.RELEASE, Build.MODEL
            )
        )

        Requests(
            okHttpClient,
            defaultHeaders,
            defaultCacheTime = 30,
            defaultCacheTimeUnit = TimeUnit.MINUTES
        )
    }

    private val isPlaying = MutableStateFlow(false)
    private val isBuffering = MutableStateFlow(true)
    private val bufferedPercentage = MutableStateFlow(0)
    private val currentPlaybackPosition = MutableStateFlow(0L)
    private val mediaDuration = MutableStateFlow(0L)
    private val mediaQuality = MutableStateFlow("Undefined Quality")
    private val currentSkip = MutableStateFlow<Pair<String, Long>?>(null)
    private val selectedModule = MutableStateFlow<ModuleModel?>(null)

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use the entire screen
        enableEdgeToEdge()

        // Hide the system bars (status bar and navigation bar)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        insetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Completely remove the activity from the back stack when the user presses the back button
        onBackPressedDispatcher.addCallback {
            onDestroy()
        }

        intent?.getStringExtra(BUNDLE)?.let {
            val bundle = Json.decodeFromString<WatchBundle>(it)
            savedInstanceState?.putString(BUNDLE, it)
            extractBundle(bundle)
        } ?: throw IllegalArgumentException("UUID for Content has not been Set")

        savedInstanceState?.let {
            lifecycleScope.launch {
                val bundle = Json.decodeFromString<WatchBundle>(it.getString(BUNDLE, ""))
                extractBundle(bundle)
                isPlaying.emit(it.getBoolean(IS_PLAYING))
                currentPlaybackPosition.emit(it.getLong(PLAYBACK_POSITION))
            }
        }

        lifecycleScope.launch {
            val moduleData = applicationContext.moduleDatastore.data.firstOrNull()
            moduleUseCases.getModuleUris().find { it.id == moduleData?.selectedModuleId }
                ?.let {
                    selectedModule.emit(it)
                }
        }

        // Loop through the headers of the source and use them within the DataSource
        dataSourceFactory = DataSource.Factory {
            val httpDataSource = OkHttpDataSource.Factory(client.baseClient).createDataSource()
            sources.headers?.forEach {
                httpDataSource.setRequestProperty(it.key, it.value)
            }
            client.defaultHeaders.keys.forEach { key ->
                if (key.isBlank()) return@forEach
                if (key !in (sources.headers ?: mapOf())) {
                    client.defaultHeaders[key]?.let {
                        httpDataSource.setRequestProperty(key, it)
                    }
                }
            }
            httpDataSource
        }

        val mimeTypeSingleton = MimeTypeMap.getSingleton()

        mimeType = when (sources.sources.firstOrNull()?.type) {
            "hls" -> MimeTypes.APPLICATION_M3U8
            "dash" -> MimeTypes.APPLICATION_MPD
            else -> mimeTypeSingleton.getMimeTypeFromExtension(
                sources.sources.firstOrNull()?.file?.split(".")?.last()?.lowercase()
            ) ?: MimeTypes.APPLICATION_MP4
        }

        mediaItem = MediaItem.Builder().apply {
            setUri(sources.sources.firstOrNull()?.file ?: "")
            setMimeType(mimeType)
            sources.subtitles?.let { subs ->
                // Module dev might not have checked this properly - can't give benefit of the doubt
                if (subs.isEmpty()) return@let

                setSubtitleConfigurations(subs.map {
                    MediaItem.SubtitleConfiguration.Builder(
                        it.url.toUri()
                    ).apply {
                        // TODO: Allow user to configure the default language
                        setLanguage(it.language)
                        if (it.language.lowercase() in setOf(
                                "english", "en-us", "en", "en-gb"
                            )
                        ) {
                            setSelectionFlags(SELECTION_FLAG_DEFAULT)
                        }
                        setMimeType(
                            when (it.url.split(".").lastOrNull() ?: "") {
                                "vtt" -> MimeTypes.TEXT_VTT
                                "ass" -> MimeTypes.TEXT_SSA
                                "srt" -> MimeTypes.APPLICATION_SUBRIP
                                else -> MimeTypes.TEXT_UNKNOWN
                            }
                        )
                    }.build()
                })
            }
        }.build()

        if (!isInitialized) buildPlayer()

        setContent {
            ChoutenTheme {
                var showPlayerUI by rememberSaveable { mutableStateOf(false) }
                val playing by isPlaying.collectAsState()
                val buffering by isBuffering.collectAsState()
                val bufferedPercentage by bufferedPercentage.collectAsState()
                val position by currentPlaybackPosition.collectAsState()
                val duration by mediaDuration.collectAsState()
                val skip by currentSkip.collectAsState()
                val quality by mediaQuality.collectAsState()
                // This won't ever change as the selector is not available within this screen
                val module by selectedModule.collectAsState()

                val mediaTitle = remember(watchBundle.selectedMediaIndex) {
                    // TODO: Handle server changing
                    media.getOrNull(0)?.list?.getOrNull(watchBundle.selectedMediaIndex)?.title
                        ?: UiText.StringRes(R.string.no_title_found).string(this)
                }

                val selectedMediaIndex by remember(watchBundle) {
                    mutableIntStateOf(watchBundle.selectedMediaIndex)
                }

                val snackbarHost = remember { SnackbarHostState() }
                val snackbarLambda = { model: SnackbarModel ->
                    lifecycleScope.launch {
                        snackbarHost.showSnackbar(model)
                    }
                }

                val skipButtonBtmPadding by animateDpAsState(
                    if (showPlayerUI) 64.dp else 48.dp,
                    tween(durationMillis = 350, easing = EaseInOut),
                    "Skip Button Padding"
                )

                Scaffold(modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHost) }) { it ->
                    Box(
                        modifier = Modifier
                            .padding(it)
                            .background(Color.Black)
                            .clickable {
                                showPlayerUI = !showPlayerUI
                            },
                    ) {
                        DisposableEffect(Unit) {
                            val listener = ExoplayerEventHandler()
                            exoplayer.addListener(listener)

                            onDispose { exoplayer.removeListener(listener) }
                        }
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoplayer
                                    useController = false
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    subtitleView?.apply {
                                        setPadding(0, 0, 0, 80)
                                        setFixedTextSize(
                                            TypedValue.COMPLEX_UNIT_DIP, 20f
                                        )

                                        setStyle(
                                            CaptionStyleCompat(
                                                Color.White.toArgb(),
                                                Color.Black.copy(alpha = 0.2f).toArgb(),
                                                Color.Transparent.toArgb(),
                                                CaptionStyleCompat.EDGE_TYPE_NONE,
                                                Color.White.toArgb(),
                                                Typeface.DEFAULT_BOLD
                                            )
                                        )

                                        setApplyEmbeddedStyles(true)
                                        setApplyEmbeddedFontSizes(true)

                                        setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 1.2f)
                                    }
                                }

                            },
                        )

                        PlayerControls(
                            isVisible = { showPlayerUI },
                            isPlaying = { playing },
                            onPauseToggle = {
                                if (playing) {
                                    exoplayer.pause()
                                } else {
                                    exoplayer.play()
                                }
                            },
                            isBuffering = { buffering },
                            bufferPercentage = { bufferedPercentage },
                            currentModule = module?.name,
                            title = watchBundle.mediaTitle,
                            currentTime = { position },
                            duration = { duration },
                            qualityLabel = { quality },
                            episodeTitle = mediaTitle,
                            modifier = Modifier
                                .fillMaxSize(),
                            onBackClick = {
                                finishAndRemoveTask()
                            },
                            onForwardClick = {
                                exoplayer.seekTo(position + (10 * 1000))
                            },
                            onNextEpisode = {
                                if (selectedMediaIndex < ((media.getOrNull(0)?.list?.size)
                                        ?: 0) - 1
                                ) {
                                    watchBundle = watchBundle.copy(
                                        selectedMediaIndex = selectedMediaIndex + 1
                                    )
                                } else {
                                    snackbarLambda(
                                        SnackbarModel(
                                            message = UiText.StringRes(R.string.no_more_media)
                                                .string(this@ExoplayerActivity),
                                            isError = false
                                        )
                                    )
                                }
                            },
                            onReplayClick = {
                                exoplayer.seekTo(position - (10 * 1000))
                            },
                            onSeekChanged = { time -> exoplayer.seekTo(time.toLong()) },
                            onSettingsClick = {},
                        )

                        AnimatedVisibility(
                            visible = skip != null,
                            modifier = Modifier
                                .padding(horizontal = 48.dp, skipButtonBtmPadding)
                                .align(Alignment.BottomEnd),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            val (skipLabel, skipTime) = skip ?: return@AnimatedVisibility
                            FilledTonalButton(
                                onClick = {
                                    lifecycleScope.launch {
                                        // Reset the skip button.
                                        currentSkip.emit(null)
                                    }
                                    exoplayer.seekTo(skipTime)

                                },
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.9f),
                                    contentColor = Color.Black
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(Icons.Default.FastForward, null)
                                    Text(
                                        text = stringResource(
                                            R.string.skip_button_label,
                                            skipLabel
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun buildPlayer() {
        exoplayer = ExoPlayer.Builder(this).apply {
            setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
        }.build().apply {
            playWhenReady = true
            videoScalingMode = VIDEO_SCALING_MODE_SCALE_TO_FIT
            setMediaItem(mediaItem)
            prepare()
            seekTo(currentPlaybackPosition.value)
        }
        isInitialized = true
    }

    private fun releasePlayer() {
        lifecycleScope.launch {
            isPlaying.emit(exoplayer.isPlaying)
            currentPlaybackPosition.emit(exoplayer.currentPosition)
        }

        isInitialized = false
        exoplayer.release()
    }

    @kotlin.OptIn(ExperimentalSerializationApi::class)
    private fun extractBundle(bundle: WatchBundle) {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }

        val uuid = bundle.mediaUuid

        lifecycleScope.launch {
            // Read the files and get the data
            cacheDir.resolve("${uuid}_server.json").useLines { lines ->
                val text = lines.joinToString("\n")
                val result = json.decodeFromString<List<WatchResult.ServerData>>(text)
                servers = result
            }
            cacheDir.resolve("${uuid}_sources.json").useLines { lines ->
                val text = lines.joinToString("\n")
                val result = json.decodeFromString<WatchResult>(text)
                sources = result
            }
            cacheDir.resolve("${uuid}_media.json").useLines { lines ->
                val text = lines.joinToString("\n")
                val result = json.decodeFromString<List<InfoResult.MediaListItem>>(text)
                media = result
            }
        }
        watchBundle = bundle
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (!isInitialized) return

        lifecycleScope.launch {
            outState.putBoolean(IS_PLAYING, isPlaying.first())
            outState.putLong(PLAYBACK_POSITION, currentPlaybackPosition.value)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isInitialized) return

        exoplayer.pause()

    }

    override fun onResume() {
        super.onResume()
        if (!isInitialized) return

        exoplayer.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isInitialized) return

        releasePlayer()
        finishAndRemoveTask()
    }

    override fun onStop() {
        super.onStop()
        if (!isInitialized) return

        exoplayer.pause()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        finishAndRemoveTask()
        startActivity(intent)
    }

    inner class ExoplayerEventHandler : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)

            lifecycleScope.launch {
                isPlaying.emit(player.isPlaying)
                isBuffering.emit(player.playbackState == Player.STATE_BUFFERING)
                bufferedPercentage.emit(player.bufferedPercentage)
                currentPlaybackPosition.emit(player.currentPosition)
                mediaDuration.emit(player.duration)
                mediaQuality.emit("${player.videoSize.width}x${player.videoSize.height}")
            }
        }

        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()
            val height = exoplayer.videoSize.height
            val width = exoplayer.videoSize.width

            aspectRatio = Rational(width, height)

            if (exoplayer.duration < currentPlaybackPosition.value) {
                exoplayer.seekTo(0)
            }

            handler.postDelayed(object : Runnable {
                override fun run() {
                    lifecycleScope.launch {
                        currentPlaybackPosition.emit(exoplayer.currentPosition)
                        currentSkip.emit(null)
                        sources.skips?.firstOrNull {
                            // The modules provide the values in seconds, but the player uses milliseconds
                            // So we need to multiply / divide by 1000
                            // We add one to the start time to prevent the player from showing
                            // the popup instantly.
                            ((it.start + 1)..<it.end).contains(exoplayer.currentPosition.toDouble() / 1000)
                        }?.let {
                            // Show skip button
                            currentSkip.emit(Pair(it.type, it.end.toLong() * 1000))
                        }
                    }
                    handler.postDelayed(this, 1000)
                }
            }, 1000)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_READY) {
                exoplayer.play()
            }
        }
    }

    companion object {
        const val IS_PLAYING = "is_playing"
        const val PLAYBACK_POSITION = "playback_position"
        const val BUNDLE = "bundle"
    }
}
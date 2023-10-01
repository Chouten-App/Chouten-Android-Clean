package com.chouten.app.presentation.ui.screens.watch

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.util.TypedValue
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import com.chouten.app.presentation.ui.theme.ChoutenTheme
import com.lagradost.nicehttp.Requests
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ExoplayerActivity : ComponentActivity(), Player.Listener {

    private var aspectRatio = Rational(16, 9)
    private lateinit var exoplayer: ExoPlayer

    private val isPlaying: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private var playbackPosition = 0L

    private lateinit var dataSourceFactory: DataSource.Factory
    private lateinit var mediaItem: MediaItem

    private var isInitialized = false

    private lateinit var watchBundle: WatchBundle
    private lateinit var sources: WatchResult
    private lateinit var servers: List<WatchResult.ServerData>

    private lateinit var mimeType: String

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


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
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

        intent?.getStringExtra(UUID)?.let {
            extractBundle(it)
        } ?: throw IllegalArgumentException("UUID for Content has not been Set")

        savedInstanceState?.let {
            lifecycleScope.launch {
                extractBundle(it.getString(UUID, ""))
                isPlaying.emit(it.getBoolean(IS_PLAYING))
            }

            playbackPosition = it.getLong(PLAYBACK_POSITION)
        }

        dataSourceFactory = DataSource.Factory {
            val httpDataSource = OkHttpDataSource.Factory(client.baseClient).createDataSource()
            sources.headers?.forEach {
                httpDataSource.setRequestProperty(it.key, it.value)
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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier
                            .padding(it)
                            .background(Color.Black)
                            .clickable {
                                showPlayerUI = !showPlayerUI
                            },
                    ) {
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
            seekTo(playbackPosition)
        }
        isInitialized = true
    }

    private fun releasePlayer() {
        lifecycleScope.launch {
            isPlaying.emit(exoplayer.isPlaying)
        }
        playbackPosition = exoplayer.currentPosition

        isInitialized = false
        exoplayer.release()
    }

    @kotlin.OptIn(ExperimentalSerializationApi::class)
    private fun extractBundle(uuid: String) {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }

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
            cacheDir.resolve("${uuid}_bundle.json").useLines { lines ->
                val text = lines.joinToString("\n")
                val result = json.decodeFromString<WatchBundle>(text)
                watchBundle = result
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("ExoplayerActivity", "Saving instance state $outState")
        super.onSaveInstanceState(outState)
        if (!isInitialized) return

        outState.clear()
        lifecycleScope.launch {
            outState.putBoolean(IS_PLAYING, isPlaying.first())
            outState.putLong(PLAYBACK_POSITION, playbackPosition)
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

    override fun onEvents(player: Player, events: Player.Events) {
        super.onEvents(player, events)

        lifecycleScope.launch {
            isPlaying.emit(player.isPlaying)
        }
        playbackPosition = player.currentPosition
    }

    override fun onRenderedFirstFrame() {
        super.onRenderedFirstFrame()
        val height = exoplayer.videoSize.height
        val width = exoplayer.videoSize.width

        aspectRatio = Rational(width, height)

        if (exoplayer.duration < playbackPosition) {
            exoplayer.seekTo(0)
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_READY) {
            exoplayer.play()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        finishAndRemoveTask()
        startActivity(intent)
    }

    companion object {
        const val IS_PLAYING = "is_playing"
        const val PLAYBACK_POSITION = "playback_position"
        const val UUID = "uuid"
    }
}
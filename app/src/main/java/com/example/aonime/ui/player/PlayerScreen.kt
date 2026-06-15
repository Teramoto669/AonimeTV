@file:kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.media3.common.util.UnstableApi::class)
package com.example.aonime.ui.player

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import com.example.aonime.SubtitleProcessor
import com.example.aonime.data.SubtitleTrack
import com.example.aonime.theme.TextPrimary
import com.example.aonime.theme.TextSecondary
import com.example.aonime.theme.Violet
import com.example.aonime.theme.VioletLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    slug: String,
    ep: String,
    title: String,
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(slug, ep) { viewModel.load(slug, ep) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        when (val state = uiState) {
            PlayerUiState.Loading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = Violet)
                    Spacer(Modifier.height(12.dp))
                    Text("Loading stream...", color = TextSecondary, fontSize = 14.sp)
                }
            }
            is PlayerUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Stream unavailable", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(state.message, color = TextSecondary, fontSize = 13.sp)
                }
            }
            is PlayerUiState.Ready -> {
                VideoPlayer(
                    streamUrl = state.streamUrl,
                    subtitles = state.subtitles,
                    availableSources = state.availableSources,
                    activeSource = state.activeSource,
                    onServerSelected = { viewModel.selectSource(it) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayer(
    streamUrl: String,
    subtitles: List<SubtitleTrack>,
    availableSources: List<com.example.aonime.data.WatchSource>,
    activeSource: com.example.aonime.data.WatchSource?,
    onServerSelected: (com.example.aonime.data.WatchSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Player States
    var showSettings by remember { mutableStateOf(false) }
    var videoQualities by remember { mutableStateOf(emptyList<Int>()) }
    var isControllerVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isDragging by remember { mutableStateOf(false) }
    var resetTimerTrigger by remember { mutableLongStateOf(0L) }

    fun resetHideTimer() {
        resetTimerTrigger = System.currentTimeMillis()
    }

    // Settings States
    var subSizeScale by remember { mutableFloatStateOf(1.0f) }
    var subDelaySec by remember { mutableFloatStateOf(0f) }
    var subColor by remember { mutableStateOf(android.graphics.Color.WHITE) }
    var edgeStyle by remember { mutableStateOf(CaptionStyleCompat.EDGE_TYPE_OUTLINE) }
    var selectedSubIdx by remember { mutableIntStateOf(if (subtitles.isNotEmpty()) 0 else -1) }
    var selectedQualityHeight by remember { mutableStateOf<Int?>(null) }
    var isPlayerViewReady by remember { mutableStateOf(false) }

    LaunchedEffect(streamUrl) {
        selectedSubIdx = if (subtitles.isNotEmpty()) 0 else -1
        subDelaySec = 0f
    }

    val exoPlayer = remember {
        val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                trackSelectionParameters = trackSelectionParameters.buildUpon()
                    .setPreferredTextLanguage("en")
                    .setSelectUndeterminedTextLanguage(true)
                    .build()
                playWhenReady = true
            }
    }

    var playerViewInstance by remember { mutableStateOf<PlayerView?>(null) }

    // Auto-hide controller after 3 seconds (but NOT while Settings modal is open or user is dragging)
    LaunchedEffect(isControllerVisible, isPlaying, showSettings, isDragging, resetTimerTrigger) {
        if (isControllerVisible && isPlaying && !showSettings && !isDragging) {
            delay(3000)
            isControllerVisible = false
        }
    }

    // Poll player position for progress bar
    LaunchedEffect(exoPlayer, isDragging) {
        while (true) {
            if (!isDragging) {
                currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
                duration = exoPlayer.duration.coerceAtLeast(1L)
            }
            delay(500)
        }
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                val heights = mutableListOf<Int>()
                tracks.groups.forEach { group ->
                    if (group.type == C.TRACK_TYPE_VIDEO) {
                        for (i in 0 until group.length) {
                            val format = group.getTrackFormat(i)
                            if (format.height > 0 && !heights.contains(format.height)) {
                                heights.add(format.height)
                            }
                        }
                    }
                }
                videoQualities = heights.sortedDescending()
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Apply subtitle and media item changes
    LaunchedEffect(streamUrl, selectedSubIdx, subDelaySec) {
        val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
        val playWhenReady = exoPlayer.playWhenReady

        if (selectedSubIdx >= 0 && selectedSubIdx < subtitles.size) {
            val track = subtitles[selectedSubIdx]
            val subUrl = track.proxyUrl ?: track.file ?: ""
            if (subUrl.isNotBlank()) {
                val shiftedUrl = SubtitleProcessor.processAndShiftSubtitle(context, subUrl, subDelaySec)
                val subtitleConfig = shiftedUrl?.let {
                    MediaItem.SubtitleConfiguration.Builder(Uri.parse(it))
                        .setMimeType(MimeTypes.TEXT_VTT)
                        .setLanguage("en")
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build()
                }

                val mediaItemBuilder = MediaItem.Builder()
                    .setUri(streamUrl)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)

                if (subtitleConfig != null) {
                    mediaItemBuilder.setSubtitleConfigurations(listOf(subtitleConfig))
                }

                exoPlayer.setMediaItem(mediaItemBuilder.build())
            }
        } else {
            val mediaItem = MediaItem.Builder()
                .setUri(streamUrl)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build()
            exoPlayer.setMediaItem(mediaItem)
        }

        exoPlayer.seekTo(pos)
        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.prepare()
    }

    // Apply Video Quality changes
    LaunchedEffect(selectedQualityHeight) {
        if (selectedQualityHeight == null) {
            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                .buildUpon()
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                .build()
        } else {
            exoPlayer.currentTracks.groups.forEach { group ->
                if (group.type == C.TRACK_TYPE_VIDEO) {
                    for (i in 0 until group.length) {
                        if (group.getTrackFormat(i).height == selectedQualityHeight) {
                            val override = TrackSelectionOverride(group.mediaTrackGroup, listOf(i))
                            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                .buildUpon()
                                .setOverrideForType(override)
                                .build()
                            return@forEach
                        }
                    }
                }
            }
        }
    }

    // Apply Caption Style changes
    LaunchedEffect(subSizeScale, subColor, edgeStyle, isPlayerViewReady) {
        playerViewInstance?.subtitleView?.let { subtitleView ->
            val style = CaptionStyleCompat(
                subColor,
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
                edgeStyle,
                android.graphics.Color.BLACK,
                null
            )
            val paddingBottomPx = android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, 24f, context.resources.displayMetrics
            ).toInt()

            subtitleView.setStyle(style)
            subtitleView.setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * subSizeScale)
            subtitleView.setBottomPaddingFraction(0.08f)
            subtitleView.setPadding(0, 0, 0, paddingBottomPx)
            subtitleView.setApplyEmbeddedStyles(false)
        }
    }

    // D-pad / key events that should reveal the controller
    val dpadKeyCodes = remember {
        setOf(
            android.view.KeyEvent.KEYCODE_DPAD_UP,
            android.view.KeyEvent.KEYCODE_DPAD_DOWN,
            android.view.KeyEvent.KEYCODE_DPAD_LEFT,
            android.view.KeyEvent.KEYCODE_DPAD_RIGHT,
            android.view.KeyEvent.KEYCODE_DPAD_CENTER,
            android.view.KeyEvent.KEYCODE_ENTER,
            android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            android.view.KeyEvent.KEYCODE_MEDIA_PLAY,
            android.view.KeyEvent.KEYCODE_MEDIA_PAUSE,
        )
    }

    val focusRequester = remember { FocusRequester() }
    val playPauseFocusRequester = remember { FocusRequester() }
    val settingsCloseFocusRequester = remember { FocusRequester() }
    var isFirstRun by remember { mutableStateOf(true) }

    // Request focus to the Compose layer so D-Pad events arrive here (not stolen by PlayerView)
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // When controller becomes visible, auto-focus the Play/Pause button
    LaunchedEffect(isControllerVisible) {
        if (isControllerVisible) {
            delay(250) // wait for AnimatedVisibility to finish entering
            try { playPauseFocusRequester.requestFocus() } catch (_: Exception) {}
        } else {
            // Return focus to outer box so D-Pad can wake controller again
            try { focusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    LaunchedEffect(showSettings) {
        if (showSettings) {
            delay(100)
            try {
                settingsCloseFocusRequester.requestFocus()
            } catch (_: Exception) {}
        } else if (!isFirstRun) {
            try {
                playPauseFocusRequester.requestFocus()
            } catch (_: Exception) {}
        } else {
            isFirstRun = false
        }
    }

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable(enabled = !showSettings)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown &&
                    keyEvent.key.nativeKeyCode in dpadKeyCodes
                ) {
                    resetHideTimer()
                    if (!isControllerVisible) {
                        isControllerVisible = true
                        true  // consume: don't propagate to hidden buttons
                    } else {
                        false // controller is visible, let buttons handle it
                    }
                } else {
                    false
                }
            }
    ) {
        // Video surface - no built-in controller
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    useController = false  // We use our own custom Compose controller
                    // Disable focus so D-Pad key events go to Compose instead of this View
                    isFocusable = false
                    isFocusableInTouchMode = false
                    playerViewInstance = this
                    isPlayerViewReady = true
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Tap anywhere to show/hide controller
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        isControllerVisible = !isControllerVisible
                    })
                }
        )

        // Custom Compose Controller Overlay
        AnimatedVisibility(
            visible = isControllerVisible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .focusProperties { canFocus = !showSettings }
            ) {
                // Settings button top-right
                var isSettingsFocused by remember { mutableStateOf(false) }
                ControllerIconButton(
                    icon = Icons.Rounded.Settings,
                    onClick = { showSettings = true },
                    size = 32.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                )

                // Center playback controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Previous (disabled visually but focusable)
                    ControllerIconButton(
                        icon = Icons.Rounded.SkipPrevious,
                        onClick = {
                            resetHideTimer()
                            exoPlayer.seekToPreviousMediaItem()
                        },
                        size = 36.dp,
                    )
                    // Rewind 5s
                    ControllerIconButton(
                        icon = Icons.Rounded.FastRewind,
                        label = "5",
                        onClick = {
                            resetHideTimer()
                            exoPlayer.seekTo((exoPlayer.currentPosition - 5000L).coerceAtLeast(0L))
                            isControllerVisible = true
                        },
                        size = 42.dp,
                    )
                    // Play/Pause (always present, toggles icon based on state)
                    ControllerIconButton(
                        icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        onClick = {
                            resetHideTimer()
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            isControllerVisible = true
                        },
                        size = 56.dp,
                        isPrimary = true,
                        focusRequester = playPauseFocusRequester,
                    )
                    // Forward 15s
                    ControllerIconButton(
                        icon = Icons.Rounded.FastForward,
                        label = "15",
                        onClick = {
                            resetHideTimer()
                            exoPlayer.seekTo(exoPlayer.currentPosition + 15000L)
                            isControllerVisible = true
                        },
                        size = 42.dp,
                    )
                    // Next
                    ControllerIconButton(
                        icon = Icons.Rounded.SkipNext,
                        onClick = {
                            resetHideTimer()
                            exoPlayer.seekToNextMediaItem()
                        },
                        size = 36.dp,
                    )
                }

                // Bottom progress bar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    // Time display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            color = Color.White,
                            fontSize = 13.sp,
                        )
                        Text(
                            text = formatTime(duration),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    // Progress bar (seekable)
                    var isSliderFocused by remember { mutableStateOf(false) }
                    val thumbWidth by animateDpAsState(
                        targetValue = if (isSliderFocused) 3.dp else 0.dp,
                        animationSpec = tween(200),
                        label = "thumbWidth"
                    )
                    val thumbHeight by animateDpAsState(
                        targetValue = if (isSliderFocused) 14.dp else 0.dp,
                        animationSpec = tween(200),
                        label = "thumbHeight"
                    )
                    val trackHeight by animateDpAsState(
                        targetValue = if (isSliderFocused) 6.dp else 4.dp,
                        animationSpec = tween(200),
                        label = "trackHeight"
                    )
                    Slider(
                        value = currentPosition.toFloat().coerceIn(0f, duration.toFloat().coerceAtLeast(1f)),
                        onValueChange = { newValue ->
                            isDragging = true
                            currentPosition = newValue.toLong()
                        },
                        onValueChangeFinished = {
                            exoPlayer.seekTo(currentPosition)
                            isDragging = false
                        },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = if (isSliderFocused) VioletLight else Violet,
                            activeTrackColor = if (isSliderFocused) VioletLight else Violet,
                            inactiveTrackColor = Color.White.copy(alpha = if (isSliderFocused) 0.5f else 0.3f)
                        ),
                        thumb = {
                            Box(
                                modifier = Modifier.size(width = 20.dp, height = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = thumbWidth, height = thumbHeight)
                                        .background(VioletLight, RoundedCornerShape(1.dp))
                                )
                            }
                        },
                        track = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(trackHeight)
                                    .clip(RoundedCornerShape(trackHeight / 2))
                                    .background(Color.White.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(currentPosition.toFloat() / duration.toFloat().coerceAtLeast(1f))
                                        .fillMaxHeight()
                                        .background(if (isSliderFocused) VioletLight else Violet)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isSliderFocused = it.isFocused }
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    when (keyEvent.key.nativeKeyCode) {
                                        android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                                            try {
                                                playPauseFocusRequester.requestFocus()
                                                true
                                            } catch (e: Exception) {
                                                false
                                            }
                                        }
                                        else -> false
                                    }
                                } else {
                                    false
                                }
                            }
                    )
                }
            }
        }

        // Settings Modal Overlay
        if (showSettings) {
            BackHandler(enabled = showSettings) {
                showSettings = false
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { showSettings = false })
                    },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxHeight(0.9f)
                        .fillMaxWidth(0.9f)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {})
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Player Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            IconButton(
                                onClick = { showSettings = false },
                                modifier = Modifier.focusRequester(settingsCloseFocusRequester)
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        Divider(color = Color.DarkGray)

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Server Selection
                            if (availableSources.isNotEmpty()) {
                                Text("Server", color = Violet, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    availableSources.forEach { source ->
                                        val serverName = source.server ?: "Unknown"
                                        val typeName = source.type?.let { " (${it.uppercase()})" } ?: ""
                                        val name = "$serverName$typeName"
                                        val isSelected = source == activeSource
                                        QualityButton(name, isSelected) {
                                            onServerSelected(source)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // Quality
                            Text("Video Quality", color = Violet, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                QualityButton("Auto", selectedQualityHeight == null) { selectedQualityHeight = null }
                                videoQualities.forEach { height ->
                                    QualityButton("${height}p", selectedQualityHeight == height) { selectedQualityHeight = height }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Subtitle Track
                            Text("Subtitle Track", color = Violet, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                QualityButton("Off", selectedSubIdx == -1) { selectedSubIdx = -1 }
                                subtitles.forEachIndexed { index, track ->
                                    QualityButton(track.label ?: "Track ${index + 1}", selectedSubIdx == index) { selectedSubIdx = index }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Subtitle Sync
                            Text("Subtitle Sync Delay (s): ${if(subDelaySec == 0f) "Synced" else String.format("%+.1f", subDelaySec)}", color = Violet, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                QualityButton("-0.5s", false) { subDelaySec -= 0.5f }
                                QualityButton("-0.1s", false) { subDelaySec -= 0.1f }
                                QualityButton("Reset", false) { subDelaySec = 0f }
                                QualityButton("+0.1s", false) { subDelaySec += 0.1f }
                                QualityButton("+0.5s", false) { subDelaySec += 0.5f }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Subtitle Size
                            Text("Subtitle Size: ${(subSizeScale * 100).roundToInt()}%", color = Violet, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { scale ->
                                    QualityButton("${(scale * 100).roundToInt()}%", subSizeScale == scale) { subSizeScale = scale }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Text Color
                            Text("Text Color", color = Violet, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                ColorButton(android.graphics.Color.WHITE, subColor == android.graphics.Color.WHITE) { subColor = android.graphics.Color.WHITE }
                                ColorButton(android.graphics.Color.YELLOW, subColor == android.graphics.Color.YELLOW) { subColor = android.graphics.Color.YELLOW }
                                ColorButton(android.graphics.Color.CYAN, subColor == android.graphics.Color.CYAN) { subColor = android.graphics.Color.CYAN }
                                ColorButton(android.graphics.Color.GREEN, subColor == android.graphics.Color.GREEN) { subColor = android.graphics.Color.GREEN }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Edge Style
                            Text("Edge Style", color = Violet, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                QualityButton("None", edgeStyle == CaptionStyleCompat.EDGE_TYPE_NONE) { edgeStyle = CaptionStyleCompat.EDGE_TYPE_NONE }
                                QualityButton("Outline", edgeStyle == CaptionStyleCompat.EDGE_TYPE_OUTLINE) { edgeStyle = CaptionStyleCompat.EDGE_TYPE_OUTLINE }
                                QualityButton("Drop Shadow", edgeStyle == CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW) { edgeStyle = CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW }
                                QualityButton("Raised", edgeStyle == CaptionStyleCompat.EDGE_TYPE_RAISED) { edgeStyle = CaptionStyleCompat.EDGE_TYPE_RAISED }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Format milliseconds to mm:ss */
private fun formatTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0L)
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%02d:%02d".format(min, sec)
}

/**
 * A TV-friendly icon button that is ALWAYS focusable regardless of player state.
 */
@Composable
private fun ControllerIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    label: String? = null,
    isPrimary: Boolean = false,
    focusRequester: FocusRequester? = null,
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.18f else 1f,
        animationSpec = tween(150),
        label = "btnScale"
    )

    val focusModifier = if (focusRequester != null) {
        Modifier.focusRequester(focusRequester)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(focusModifier)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(
                when {
                    isPrimary && isFocused -> Violet
                    isPrimary -> Color.White.copy(alpha = 0.15f)
                    isFocused -> Violet.copy(alpha = 0.85f)
                    else -> Color.White.copy(alpha = 0.08f)
                }
            )
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) VioletLight else Color.Transparent,
                shape = CircleShape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick,
            )
            .size(if (isPrimary) size + 8.dp else size)
            .padding(if (isPrimary) 10.dp else (if (label != null) 2.dp else 8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (label != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(size * 0.4f)
                    .align(Alignment.Center)
                    .offset(y = (-3).dp),
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 1.dp)
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}


@Composable
private fun QualityButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = tween(150),
        label = "qualityScale"
    )
    Button(
        onClick = onClick,
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) com.example.aonime.theme.FocusedBorder else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Violet else Color.DarkGray,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text)
    }
}

@Composable
private fun ColorButton(colorInt: Int, isSelected: Boolean, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.15f else 1f,
        animationSpec = tween(150),
        label = "colorScale"
    )
    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(Color(colorInt), RoundedCornerShape(20.dp))
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) com.example.aonime.theme.FocusedBorder else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.padding(2.dp).background(Color.Transparent, RoundedCornerShape(20.dp))
                else Modifier
            )
    ) {
        if (isSelected) {
            Box(modifier = Modifier.fillMaxSize().padding(4.dp).background(Color.Black.copy(alpha=0.3f), RoundedCornerShape(20.dp)))
        }
    }
}

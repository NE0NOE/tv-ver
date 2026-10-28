package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.FocusZone
import com.example.model.KioskScreenMode
import com.example.model.SignageLane
import com.example.model.SignageMediaItem
import com.example.player.GaplessSignagePlayerManager
import com.example.ui.components.ApexFocusableMediaCard
import com.example.ui.components.ApexKioskOverrideModal
import com.example.ui.components.ApexRemoteActionButton
import com.example.ui.components.ApexRemoteLegendBar
import com.example.ui.components.ApexStatusBadge
import com.example.ui.components.GaplessVideoStage
import com.example.ui.theme.ApexInactiveTileBorder
import com.example.ui.theme.ApexPrimaryHighLum
import com.example.ui.theme.ApexPrimaryMauve
import com.example.ui.theme.ApexSecondaryContainer
import com.example.ui.theme.ApexSecondaryRoseSlate
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexSurfaceContainer
import com.example.ui.theme.ApexSurfaceContainerHigh
import com.example.ui.theme.ApexSurfaceContainerLow
import com.example.ui.theme.ApexTertiaryLight
import com.example.ui.theme.ApexTertiaryLive
import com.example.ui.theme.ApexTertiaryLiveTint
import com.example.ui.theme.ApexTextMutedMetadata
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexWarningAmber
import com.example.viewmodel.SignageUiState
import com.example.viewmodel.SignageViewModel

@Composable
fun ApexSignageRootScreen(
    viewModel: SignageViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val preCachedIds by viewModel.playerManager.preCachedIds.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ApexSurface)
            .drawWithContent {
                drawContent()
                // Optional 4rem (64px) Title-Safe Overscan Zone guide for commercial TV panels
                if (uiState.showOverscanGuide) {
                    val marginPx = 28.dp.toPx()
                    drawRoundRect(
                        color = ApexWarningAmber,
                        topLeft = Offset(marginPx, marginPx),
                        size = Size(
                            (size.width - marginPx * 2).coerceAtLeast(0f),
                            (size.height - marginPx * 2).coerceAtLeast(0f)
                        ),
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
                        )
                    )
                }
            }
    ) {
        when (uiState.screenMode) {
            KioskScreenMode.DASHBOARD_10FT -> {
                ApexDashboard10FootScreen(
                    uiState = uiState,
                    playerManager = viewModel.playerManager,
                    preCachedIds = preCachedIds,
                    onTopActionClick = viewModel::executeTopAction,
                    onMediaCardClick = { item, laneIdx, cardIdx ->
                        if (uiState.activePlayingItem.id == item.id &&
                            uiState.focusedLaneIndex == laneIdx &&
                            uiState.focusedCardIndex == cardIdx
                        ) {
                            viewModel.enterFullscreenPlayback()
                        } else {
                            viewModel.selectAndPlayMediaItem(item, laneIdx, cardIdx)
                        }
                    },
                    onOkKey = viewModel::onDpadOk,
                    onLeftKey = viewModel::onDpadLeft,
                    onUpKey = viewModel::onDpadUp,
                    onDownKey = viewModel::onDpadDown,
                    onRightKey = viewModel::onDpadRight,
                    onMenuKey = viewModel::onMenuKey,
                    onBackKey = { viewModel.onBackKey() }
                )
            }

            KioskScreenMode.FULLSCREEN_PLAYBACK -> {
                ApexFullscreenKioskPlaybackScreen(
                    uiState = uiState,
                    playerManager = viewModel.playerManager,
                    onPreviousTrack = viewModel.playerManager::stepToPreviousTrack,
                    onTogglePlayPause = viewModel.playerManager::togglePlayPause,
                    onNextTrack = viewModel.playerManager::stepToNextTrack,
                    onOpenMenu = viewModel::onMenuKey,
                    onExitFullscreen = viewModel::exitFullscreenPlayback
                )
            }
        }

        // Modal Dialog & Override Overlay (Level 3 Scrim + 2px #966A84 border)
        if (uiState.isModalOpen) {
            BackHandler {
                viewModel.onBackKey()
            }
            ApexKioskOverrideModal(
                focusedIndex = uiState.focusedModalIndex,
                overscanGuideActive = uiState.showOverscanGuide,
                ramUsedMb = uiState.telemetry.ramBufferUsedMb,
                ramCapMb = uiState.telemetry.ramBufferCapMb,
                diskCacheUsedMb = uiState.telemetry.diskCacheUsedMb,
                diskCacheCapMb = uiState.telemetry.diskCacheCapMb,
                onActionClick = viewModel::executeModalAction,
                onDismiss = { viewModel.onBackKey() }
            )
        }
    }
}

@Composable
fun ApexDashboard10FootScreen(
    uiState: SignageUiState,
    playerManager: GaplessSignagePlayerManager,
    preCachedIds: Set<String>,
    onTopActionClick: (Int) -> Unit,
    onMediaCardClick: (SignageMediaItem, Int, Int) -> Unit,
    onOkKey: () -> Unit,
    onLeftKey: () -> Unit,
    onUpKey: () -> Unit,
    onDownKey: () -> Unit,
    onRightKey: () -> Unit,
    onMenuKey: () -> Unit,
    onBackKey: () -> Unit
) {
    Scaffold(
        containerColor = ApexSurface,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            // Persistent Remote Legend Bar inside bottom safe zone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ApexSurface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                ApexRemoteLegendBar(
                    onOkClick = onOkKey,
                    onLeftClick = onLeftKey,
                    onUpClick = onUpKey,
                    onDownClick = onDownKey,
                    onRightClick = onRightKey,
                    onMenuClick = onMenuKey,
                    onBackClick = onBackKey
                )
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 720.dp
            val horizontalSafePadding = if (isWideScreen) 32.dp else 18.dp

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("dashboard_lazy_column"),
                contentPadding = PaddingValues(
                    start = horizontalSafePadding,
                    end = horizontalSafePadding,
                    top = 16.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // 1. Top 10-Foot Kiosk Header Bar
                item {
                    KioskHeaderBar(
                        uiState = uiState,
                        isWideScreen = isWideScreen
                    )
                }

                // 2. Hero Live Video Playback Monitor & Gapless Control Deck
                item {
                    HeroPlaybackStageAndControlDeck(
                        uiState = uiState,
                        playerManager = playerManager,
                        isWideScreen = isWideScreen,
                        onTopActionClick = onTopActionClick
                    )
                }

                // 3. Directional 2D Media Schedule Carousels (Gutter-TV: 24dp-32dp)
                itemsIndexed(uiState.lanes, key = { _, lane -> lane.id }) { laneIndex, lane ->
                    SignageScheduleCarouselLane(
                        lane = lane,
                        laneIndex = laneIndex,
                        uiState = uiState,
                        preCachedIds = preCachedIds,
                        isWideScreen = isWideScreen,
                        onCardClick = { item, cardIdx ->
                            onMediaCardClick(item, laneIndex, cardIdx)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KioskHeaderBar(
    uiState: SignageUiState,
    isWideScreen: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ApexSurfaceContainer,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, ApexInactiveTileBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(ApexPrimaryMauve)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "10-FOOT TV KIOSK",
                                style = MaterialTheme.typography.labelMedium,
                                color = ApexTextPrimary
                            )
                        }
                        Text(
                            text = "APEX SIGNAGE // NODE 04-A",
                            style = MaterialTheme.typography.labelLarge,
                            color = ApexPrimaryHighLum,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isWideScreen) {
                            "Showroom Digital Signage & Gapless Video Playback Engine"
                        } else {
                            "Apex Signage • Gapless TV Playback"
                        },
                        style = if (isWideScreen) {
                            MaterialTheme.typography.headlineMedium
                        } else {
                            MaterialTheme.typography.headlineSmall
                        },
                        color = ApexTextPrimary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ApexStatusBadge(status = uiState.syncStatus)
                    Text(
                        text = uiState.liveClockText,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ApexTextMutedMetadata
                    )
                }
            }

            // Android 6.0 (API 23/24) Hardware Optimization Telemetry Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ApexSurfaceContainerLow)
                    .border(1.dp, ApexInactiveTileBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ApexTertiaryLive)
                    )
                    Text(
                        text = uiState.statusBannerMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = ApexTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "ANDROID 6 PROFILE • RAM ${uiState.telemetry.ramBufferUsedMb}/${uiState.telemetry.ramBufferCapMb}MB • CACHE ${uiState.telemetry.diskCacheUsedMb}MB",
                    style = MaterialTheme.typography.labelMedium,
                    color = ApexTertiaryLight
                )
            }
        }
    }
}

@Composable
private fun HeroPlaybackStageAndControlDeck(
    uiState: SignageUiState,
    playerManager: GaplessSignagePlayerManager,
    isWideScreen: Boolean,
    onTopActionClick: (Int) -> Unit
) {
    val isTopZoneFocused = uiState.focusZone == FocusZone.TOP_ACTIONS

    if (isWideScreen) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left 7 Columns: Live Hardware SurfaceView Video Stage
            GaplessVideoStage(
                playerManager = playerManager,
                activeItem = uiState.activePlayingItem,
                nextUpItem = uiState.nextUpItem,
                transitionMode = uiState.transitionMode,
                isFullscreen = false,
                modifier = Modifier
                    .weight(0.58f)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(28.dp))
                    .border(2.dp, ApexPrimaryMauve, RoundedCornerShape(28.dp))
            )

            // Right 5 Columns: Gapless Control Deck & Next-Up Pre-Buffer Inspector
            ControlDeckPanel(
                uiState = uiState,
                isTopZoneFocused = isTopZoneFocused,
                onTopActionClick = onTopActionClick,
                modifier = Modifier.weight(0.42f)
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GaplessVideoStage(
                playerManager = playerManager,
                activeItem = uiState.activePlayingItem,
                nextUpItem = uiState.nextUpItem,
                transitionMode = uiState.transitionMode,
                isFullscreen = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, ApexPrimaryMauve, RoundedCornerShape(24.dp))
            )

            ControlDeckPanel(
                uiState = uiState,
                isTopZoneFocused = isTopZoneFocused,
                onTopActionClick = onTopActionClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ControlDeckPanel(
    uiState: SignageUiState,
    isTopZoneFocused: Boolean,
    onTopActionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ApexSurfaceContainer,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, ApexInactiveTileBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GAPLESS PLAYBACK CONTROLLER",
                    style = MaterialTheme.typography.labelLarge,
                    color = ApexPrimaryHighLum
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(ApexTertiaryLiveTint)
                        .border(1.dp, ApexTertiaryLive, RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ZERO LOADING SCREEN",
                        style = MaterialTheme.typography.labelMedium,
                        color = ApexTertiaryLight
                    )
                }
            }

            // Next-Up Pre-Buffered Segment Card (Shows why there is never a loading screen between videos)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ApexSurfaceContainerLow)
                    .border(1.dp, ApexSecondaryRoseSlate, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "UP NEXT IN RING BUFFER (PRE-DECODED)",
                        style = MaterialTheme.typography.labelMedium,
                        color = ApexTertiaryLight
                    )
                    Text(
                        text = "READY • 0ms CUT",
                        style = MaterialTheme.typography.labelMedium,
                        color = ApexPrimaryHighLum
                    )
                }
                Text(
                    text = uiState.nextUpItem.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ApexTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${uiState.nextUpItem.codecInfo} • ${uiState.transitionMode.title}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ApexTextMutedMetadata,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Remote Action Buttons (Component 2 from Spec)
            ApexRemoteActionButton(
                text = "Launch Fullscreen Video Mode",
                legendKey = "OK",
                isFocused = isTopZoneFocused && uiState.focusedTopActionIndex == 0,
                onClick = { onTopActionClick(0) },
                modifier = Modifier.fillMaxWidth(),
                testTag = "btn_launch_fullscreen"
            )

            ApexRemoteActionButton(
                text = "Pre-Warm All 8 Video Buffers",
                legendKey = "CACHE",
                isFocused = isTopZoneFocused && uiState.focusedTopActionIndex == 1,
                onClick = { onTopActionClick(1) },
                modifier = Modifier.fillMaxWidth(),
                testTag = "btn_prewarm_cache"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ApexRemoteActionButton(
                    text = "Mode: Gapless",
                    legendKey = "SYNC",
                    isFocused = isTopZoneFocused && uiState.focusedTopActionIndex == 2,
                    onClick = { onTopActionClick(2) },
                    modifier = Modifier.weight(1f),
                    testTag = "btn_switch_transition"
                )

                ApexRemoteActionButton(
                    text = "Kiosk Options",
                    legendKey = "MENU",
                    isFocused = isTopZoneFocused && uiState.focusedTopActionIndex == 3,
                    onClick = { onTopActionClick(3) },
                    modifier = Modifier.weight(1f),
                    testTag = "btn_kiosk_options"
                )
            }
        }
    }
}

@Composable
private fun SignageScheduleCarouselLane(
    lane: SignageLane,
    laneIndex: Int,
    uiState: SignageUiState,
    preCachedIds: Set<String>,
    isWideScreen: Boolean,
    onCardClick: (SignageMediaItem, Int) -> Unit
) {
    val listState = rememberLazyListState()
    val isLaneFocused = uiState.focusZone == FocusZone.PLAYLIST_LANES && uiState.focusedLaneIndex == laneIndex

    // Auto-scroll carousel smoothly when D-Pad focus moves along this lane
    LaunchedEffect(isLaneFocused, uiState.focusedCardIndex) {
        if (isLaneFocused) {
            listState.animateScrollToItem(uiState.focusedCardIndex.coerceIn(0, lane.items.lastIndex))
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Lane Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = lane.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isLaneFocused) ApexPrimaryHighLum else ApexTextPrimary
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ApexSecondaryContainer)
                    .border(1.dp, ApexSecondaryRoseSlate, RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = lane.badgeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = ApexTextPrimary
                )
            }
        }

        // Horizontal Carousel with uniform 2rem (28dp) TV gutters
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("carousel_${lane.id}")
        ) {
            itemsIndexed(lane.items, key = { _, item -> item.id }) { cardIndex, item ->
                val isFocused = isLaneFocused && uiState.focusedCardIndex == cardIndex
                val isPlaying = uiState.activePlayingItem.id == item.id
                val isPreCached = preCachedIds.contains(item.id) || item.preCachePriority == 1

                ApexFocusableMediaCard(
                    item = item,
                    isFocused = isFocused,
                    isPlaying = isPlaying,
                    isPreCached = isPreCached,
                    onClick = { onCardClick(item, cardIndex) },
                    modifier = Modifier.width(if (isWideScreen) 330.dp else 286.dp)
                )
            }
        }
    }
}

/**
 * Full-Screen Kiosk Video Playback Mode ("modo de reproduccion de video")
 * - Eliminates loading screens and buffering spinners between videos via pre-buffered queue & frame-hold shield.
 * - Includes BackHandler to return to the 10-foot dashboard cleanly.
 * - Includes 10-foot remote control deck at the bottom to test instant track switching (0ms cut).
 */
@Composable
fun ApexFullscreenKioskPlaybackScreen(
    uiState: SignageUiState,
    playerManager: GaplessSignagePlayerManager,
    onPreviousTrack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onOpenMenu: () -> Unit,
    onExitFullscreen: () -> Unit
) {
    val isPlaying by playerManager.isPlaying.collectAsState()

    BackHandler {
        onExitFullscreen()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexSurface)
            .testTag("fullscreen_playback_screen")
    ) {
        // Fullscreen Hardware SurfaceView Video Stage
        GaplessVideoStage(
            playerManager = playerManager,
            activeItem = uiState.activePlayingItem,
            nextUpItem = uiState.nextUpItem,
            transitionMode = uiState.transitionMode,
            isFullscreen = true,
            modifier = Modifier.fillMaxSize()
        )

        // Floating 10-Foot Kiosk Deck anchored inside bottom title-safe zone
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            color = ApexSurfaceContainer.copy(alpha = 0.94f),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(2.dp, ApexPrimaryMauve)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "KIOSK FULLSCREEN PLAYBACK • ZERO-BUFFERING ENGINE",
                            style = MaterialTheme.typography.labelMedium,
                            color = ApexPrimaryHighLum
                        )
                        Text(
                            text = "Next Pre-Loaded: ${uiState.nextUpItem.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ApexTertiaryLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    ApexStatusBadge(status = uiState.syncStatus)
                }

                // Instant Track Switch & Remote Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ApexRemoteActionButton(
                        text = "Prev Video (0ms)",
                        legendKey = "◄",
                        isFocused = false,
                        onClick = onPreviousTrack,
                        modifier = Modifier.weight(1f),
                        testTag = "fullscreen_btn_prev"
                    )

                    ApexRemoteActionButton(
                        text = if (isPlaying) "Pause Loop" else "Resume Loop",
                        legendKey = "OK",
                        isFocused = true,
                        onClick = onTogglePlayPause,
                        modifier = Modifier.weight(1.1f),
                        testTag = "fullscreen_btn_play_pause"
                    )

                    ApexRemoteActionButton(
                        text = "Next Video (0ms)",
                        legendKey = "►",
                        isFocused = false,
                        onClick = onNextTrack,
                        modifier = Modifier.weight(1f),
                        testTag = "fullscreen_btn_next"
                    )

                    ApexRemoteActionButton(
                        text = "Dashboard",
                        legendKey = "BACK",
                        isFocused = false,
                        onClick = onExitFullscreen,
                        modifier = Modifier.weight(1f),
                        testTag = "fullscreen_btn_exit"
                    )
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.example.model.KioskScreenMode
import com.example.model.SignageLane
import com.example.model.SignageMediaItem
import com.example.model.TransitionEngineMode
import com.example.player.GaplessSignagePlayerManager
import com.example.ui.components.ApexFocusableMediaCard
import com.example.ui.components.ApexKioskOverrideModal
import com.example.ui.components.ApexMinimalIconButton
import com.example.ui.components.ApexStatusBadge
import com.example.ui.components.GaplessVideoStage
import com.example.ui.components.apexDpadFocusRing
import com.example.ui.theme.ApexInactiveTileBorder
import com.example.ui.theme.ApexPrimaryHighLum
import com.example.ui.theme.ApexPrimaryMauve
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexSurfaceContainer
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
                ApexMinimalDashboardScreen(
                    uiState = uiState,
                    playerManager = viewModel.playerManager,
                    preCachedIds = preCachedIds,
                    onTopActionClick = viewModel::executeTopAction,
                    onHeroActionClick = viewModel::executeHeroAction,
                    onMediaCardClick = { item, laneIdx, cardIdx ->
                        if (uiState.activePlayingItem.id == item.id &&
                            uiState.focusRow == laneIdx + 2 &&
                            uiState.focusCol == cardIdx
                        ) {
                            viewModel.enterFullscreenPlayback()
                        } else {
                            viewModel.selectAndPlayMediaItem(item, laneIdx, cardIdx)
                        }
                    }
                )
            }

            KioskScreenMode.FULLSCREEN_PLAYBACK -> {
                ApexFullscreenKioskPlaybackScreen(
                    uiState = uiState,
                    playerManager = viewModel.playerManager,
                    onHudActionClick = viewModel::executeFullscreenHudAction,
                    onToggleHud = viewModel::onDpadOk,
                    onExitFullscreen = viewModel::exitFullscreenPlayback
                )
            }
        }

        if (uiState.isModalOpen) {
            BackHandler {
                viewModel.onBackKey()
            }
            ApexKioskOverrideModal(
                focusedIndex = uiState.focusedModalIndex,
                overscanGuideActive = uiState.showOverscanGuide,
                onActionClick = viewModel::executeModalAction,
                onDismiss = { viewModel.onBackKey() }
            )
        }
    }
}

/**
 * Ultra-Minimalist 10-Foot Dashboard:
 * - Zero on-screen remote legend bars or verbose text blocks.
 * - Every interactive element is organized in a strict 2D matrix (Rows 0..3) so the user can
 *   reach every option using only UP, DOWN, LEFT, RIGHT, and OK:
 *   - Row 0: Minimalist Top Icon Strip (Fullscreen, Play/Pause, Pre-Cache, Gapless Mode, Settings)
 *   - Row 1: Hero Live Video Stage + Prev/Next quick pills
 *   - Row 2: Primary Video Lane (16:9 visual cards)
 *   - Row 3: Secondary Video Lane (16:9 visual cards)
 */
@Composable
fun ApexMinimalDashboardScreen(
    uiState: SignageUiState,
    playerManager: GaplessSignagePlayerManager,
    preCachedIds: Set<String>,
    onTopActionClick: (Int) -> Unit,
    onHeroActionClick: (Int) -> Unit,
    onMediaCardClick: (SignageMediaItem, Int, Int) -> Unit
) {
    val isPlaying by playerManager.isPlaying.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll vertical layout smoothly as D-Pad UP/DOWN moves between Rows 0..3
    LaunchedEffect(uiState.focusRow) {
        val targetItem = when (uiState.focusRow) {
            0, 1 -> 0
            2 -> 2
            3 -> 3
            else -> 0
        }
        listState.animateScrollToItem(targetItem)
    }

    Scaffold(
        containerColor = ApexSurface,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 700.dp
            val sidePadding = if (isWideScreen) 32.dp else 16.dp

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("dashboard_lazy_column"),
                contentPadding = PaddingValues(
                    start = sidePadding,
                    end = sidePadding,
                    top = 14.dp,
                    bottom = 28.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // ROW 0: Minimalist Top Icon Bar (Navigable via LEFT / RIGHT when focusRow == 0)
                item {
                    MinimalistTopIconBar(
                        uiState = uiState,
                        isPlaying = isPlaying,
                        onTopActionClick = onTopActionClick
                    )
                }

                // ROW 1: Hero Live Video Stage (Focus col 0 = Fullscreen, col 1 = Prev, col 2 = Next)
                item {
                    MinimalistHeroStageRow(
                        uiState = uiState,
                        playerManager = playerManager,
                        isWideScreen = isWideScreen,
                        onHeroActionClick = onHeroActionClick
                    )
                }

                // ROWS 2 & 3: Pure Visual 16:9 Carousels (Navigable via UP / DOWN / LEFT / RIGHT)
                itemsIndexed(uiState.lanes, key = { _, lane -> lane.id }) { laneIndex, lane ->
                    val rowNumber = laneIndex + 2
                    MinimalistCarouselLane(
                        lane = lane,
                        isRowFocused = uiState.focusRow == rowNumber,
                        focusedCol = uiState.focusCol,
                        activePlayingId = uiState.activePlayingItem.id,
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
private fun MinimalistTopIconBar(
    uiState: SignageUiState,
    isPlaying: Boolean,
    onTopActionClick: (Int) -> Unit
) {
    val isRowFocused = uiState.focusRow == 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Minimal Status Dot
        ApexStatusBadge(status = uiState.syncStatus)

        // Right: 5 D-Pad Focusable Icon Pills (Cols 0..4)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Col 0: Fullscreen Mode
            ApexMinimalIconButton(
                icon = Icons.Default.Fullscreen,
                contentDescription = "Fullscreen Mode",
                isFocused = isRowFocused && uiState.focusCol == 0,
                onClick = { onTopActionClick(0) },
                testTag = "btn_launch_fullscreen"
            )

            // Col 1: Play / Pause
            ApexMinimalIconButton(
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                isFocused = isRowFocused && uiState.focusCol == 1,
                onClick = { onTopActionClick(1) },
                testTag = "btn_play_pause"
            )

            // Col 2: Instant Pre-Warm Cache
            ApexMinimalIconButton(
                icon = Icons.Default.Bolt,
                contentDescription = "Pre-Warm Cache",
                isActiveAccent = uiState.syncStatus == com.example.model.KioskSyncStatus.LIVE_HEALTHY,
                isFocused = isRowFocused && uiState.focusCol == 2,
                onClick = { onTopActionClick(2) },
                testTag = "btn_prewarm_cache"
            )

            // Col 3: Gapless Loop Mode
            ApexMinimalIconButton(
                icon = Icons.Default.AllInclusive,
                contentDescription = "Gapless Loop Mode",
                isActiveAccent = uiState.transitionMode == TransitionEngineMode.GAPLESS_QUEUE,
                isFocused = isRowFocused && uiState.focusCol == 3,
                onClick = { onTopActionClick(3) },
                testTag = "btn_switch_transition"
            )

            // Col 4: Options Modal
            ApexMinimalIconButton(
                icon = Icons.Default.Tune,
                contentDescription = "Options",
                isFocused = isRowFocused && uiState.focusCol == 4,
                onClick = { onTopActionClick(4) },
                testTag = "btn_kiosk_options"
            )
        }
    }
}

@Composable
private fun MinimalistHeroStageRow(
    uiState: SignageUiState,
    playerManager: GaplessSignagePlayerManager,
    isWideScreen: Boolean,
    onHeroActionClick: (Int) -> Unit
) {
    val isHeroRowFocused = uiState.focusRow == 1
    val isStageFocused = isHeroRowFocused && uiState.focusCol == 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main 16:9 Live Video Stage (Col 0 -> Press OK or Click to enter Fullscreen immediately!)
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(if (isWideScreen) 21f / 9f else 16f / 9f)
                .apexDpadFocusRing(
                    isFocused = isStageFocused,
                    scaleFactor = 1.02f,
                    cornerRadius = 28.dp
                )
                .clip(RoundedCornerShape(28.dp))
                .border(
                    width = if (isStageFocused) 3.dp else 1.5.dp,
                    color = if (isStageFocused) ApexPrimaryMauve else ApexInactiveTileBorder,
                    shape = RoundedCornerShape(28.dp)
                )
                .clickable { onHeroActionClick(0) }
                .testTag("hero_stage_fullscreen_trigger")
        ) {
            GaplessVideoStage(
                playerManager = playerManager,
                activeItem = uiState.activePlayingItem,
                nextUpItem = uiState.nextUpItem,
                transitionMode = uiState.transitionMode,
                isFullscreen = false,
                modifier = Modifier.fillMaxSize()
            )

            // Subtle Fullscreen Icon Badge in Top-Right of Hero Stage
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isStageFocused) ApexPrimaryMauve else ApexSurface.copy(alpha = 0.78f)
                    )
                    .border(
                        1.5.dp,
                        if (isStageFocused) ApexPrimaryHighLum else ApexInactiveTileBorder,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Enter Fullscreen",
                    tint = ApexPrimaryHighLum,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Quick Side Track Switchers (Col 1 = Prev Video, Col 2 = Next Video)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ApexMinimalIconButton(
                icon = Icons.Default.SkipPrevious,
                contentDescription = "Previous Video",
                isFocused = isHeroRowFocused && uiState.focusCol == 1,
                onClick = { onHeroActionClick(1) },
                testTag = "hero_btn_prev"
            )
            ApexMinimalIconButton(
                icon = Icons.Default.SkipNext,
                contentDescription = "Next Video",
                isFocused = isHeroRowFocused && uiState.focusCol == 2,
                onClick = { onHeroActionClick(2) },
                testTag = "hero_btn_next"
            )
        }
    }
}

@Composable
private fun MinimalistCarouselLane(
    lane: SignageLane,
    isRowFocused: Boolean,
    focusedCol: Int,
    activePlayingId: String,
    preCachedIds: Set<String>,
    isWideScreen: Boolean,
    onCardClick: (SignageMediaItem, Int) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(isRowFocused, focusedCol) {
        if (isRowFocused) {
            listState.animateScrollToItem(focusedCol.coerceIn(0, lane.items.lastIndex))
        }
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("carousel_${lane.id}")
    ) {
        itemsIndexed(lane.items, key = { _, item -> item.id }) { cardIndex, item ->
            val isFocused = isRowFocused && focusedCol == cardIndex
            val isPlaying = activePlayingId == item.id
            val isPreCached = preCachedIds.contains(item.id) || item.preCachePriority == 1

            ApexFocusableMediaCard(
                item = item,
                isFocused = isFocused,
                isPlaying = isPlaying,
                isPreCached = isPreCached,
                onClick = { onCardClick(item, cardIndex) },
                modifier = Modifier.width(if (isWideScreen) 280.dp else 230.dp)
            )
        }
    }
}

/**
 * Pure Fullscreen Video Playback Mode ("modo full screen"):
 * - Edge-to-edge 100% unobstructed gapless video playback by default.
 * - Navigable purely with UP / DOWN / LEFT / RIGHT / OK:
 *   - When HUD is hidden: LEFT / RIGHT switches video instantaneously (0ms gap); UP / DOWN / OK reveals the minimal icon HUD.
 *   - When HUD is visible: LEFT / RIGHT moves focus across the 5 icon pills (Exit Fullscreen, Prev, Play/Pause, Next, Options);
 *     DOWN hides the HUD; UP exits Fullscreen back to the grid.
 */
@Composable
fun ApexFullscreenKioskPlaybackScreen(
    uiState: SignageUiState,
    playerManager: GaplessSignagePlayerManager,
    onHudActionClick: (Int) -> Unit,
    onToggleHud: () -> Unit,
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
            .clickable { onToggleHud() }
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

        // Minimalist Floating Icon-Only Pill Bar (Toggled via UP/DOWN/OK or tap)
        AnimatedVisibility(
            visible = uiState.isFullscreenHudVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 24.dp)
        ) {
            Surface(
                color = ApexSurfaceContainer.copy(alpha = 0.92f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(2.dp, ApexPrimaryMauve)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Index 0: Exit Fullscreen
                    ApexMinimalIconButton(
                        icon = Icons.Default.FullscreenExit,
                        contentDescription = "Exit Fullscreen",
                        isFocused = uiState.fullscreenHudIndex == 0,
                        onClick = { onHudActionClick(0) },
                        testTag = "fullscreen_btn_exit"
                    )

                    // Index 1: Previous Video (0ms switch)
                    ApexMinimalIconButton(
                        icon = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Video",
                        isFocused = uiState.fullscreenHudIndex == 1,
                        onClick = { onHudActionClick(1) },
                        testTag = "fullscreen_btn_prev"
                    )

                    // Index 2: Play / Pause
                    ApexMinimalIconButton(
                        icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        isFocused = uiState.fullscreenHudIndex == 2,
                        onClick = { onHudActionClick(2) },
                        testTag = "fullscreen_btn_play_pause"
                    )

                    // Index 3: Next Video (0ms switch)
                    ApexMinimalIconButton(
                        icon = Icons.Default.SkipNext,
                        contentDescription = "Next Video",
                        isFocused = uiState.fullscreenHudIndex == 3,
                        onClick = { onHudActionClick(3) },
                        testTag = "fullscreen_btn_next"
                    )

                    // Index 4: Kiosk Options Modal
                    ApexMinimalIconButton(
                        icon = Icons.Default.Tune,
                        contentDescription = "Options",
                        isFocused = uiState.fullscreenHudIndex == 4,
                        onClick = { onHudActionClick(4) },
                        testTag = "fullscreen_btn_options"
                    )
                }
            }
        }
    }
}

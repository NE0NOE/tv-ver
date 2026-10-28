package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Android6HardwareTelemetry
import com.example.model.KioskScreenMode
import com.example.model.KioskSyncStatus
import com.example.model.SignageCatalogRepository
import com.example.model.SignageLane
import com.example.model.SignageMediaItem
import com.example.model.TransitionEngineMode
import com.example.player.GaplessSignagePlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Pure 2D Directional Navigation Rows (navigable strictly with UP, DOWN, LEFT, RIGHT):
 * - Row 0: Top Minimalist Icon Bar (0: Fullscreen, 1: Play/Pause, 2: Pre-Cache, 3: Loop Mode, 4: Options)
 * - Row 1: Hero Live Video Stage (0: Enter Fullscreen / Play, 1: Prev Track, 2: Next Track)
 * - Row 2: Media Lane 0 (Cards 0..3)
 * - Row 3: Media Lane 1 (Cards 0..3)
 */
data class SignageUiState(
    val lanes: List<SignageLane> = SignageCatalogRepository.lanes,
    val allItems: List<SignageMediaItem> = SignageCatalogRepository.allItems,
    val activePlayingItem: SignageMediaItem = SignageCatalogRepository.allItems.first(),
    val nextUpItem: SignageMediaItem = SignageCatalogRepository.allItems[1],
    val screenMode: KioskScreenMode = KioskScreenMode.DASHBOARD_10FT,
    val syncStatus: KioskSyncStatus = KioskSyncStatus.LIVE_HEALTHY,
    val transitionMode: TransitionEngineMode = TransitionEngineMode.GAPLESS_QUEUE,
    val telemetry: Android6HardwareTelemetry = Android6HardwareTelemetry(),
    // 2D Grid Coordinates for Dashboard (row: 0..3, col: 0..N)
    val focusRow: Int = 1, // Starts directly on the Hero Video Stage for immediate Fullscreen or navigation
    val focusCol: Int = 0,
    // Fullscreen minimalist overlay state (reachable via UP/DOWN/LEFT/RIGHT)
    val isFullscreenHudVisible: Boolean = false,
    val fullscreenHudIndex: Int = 0, // 0: Exit Fullscreen, 1: Prev, 2: Play/Pause, 3: Next, 4: Options
    // Minimalist Modal state
    val isModalOpen: Boolean = false,
    val focusedModalIndex: Int = 0, // 0: Resume (Safe Default), 1: Overscan Border, 2: Purge Cache, 3: Reset Loop
    val showOverscanGuide: Boolean = false
)

class SignageViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SignageUiState())
    val uiState: StateFlow<SignageUiState> = _uiState.asStateFlow()

    val playerManager = GaplessSignagePlayerManager(
        context = application,
        playlist = SignageCatalogRepository.allItems,
        onActiveItemChanged = { newItem, index ->
            val all = SignageCatalogRepository.allItems
            val nextIdx = (index + 1) % all.size
            _uiState.update { state ->
                state.copy(
                    activePlayingItem = newItem,
                    nextUpItem = all[nextIdx]
                )
            }
        }
    )

    init {
        observePlayerTelemetry()
    }

    private fun observePlayerTelemetry() {
        viewModelScope.launch {
            playerManager.preCachedIds.collect { cachedIds ->
                val usedDiskMb = (cachedIds.size * 14).coerceIn(18, 142)
                val status = if (cachedIds.size >= SignageCatalogRepository.allItems.size) {
                    KioskSyncStatus.LIVE_HEALTHY
                } else {
                    KioskSyncStatus.DEGRADED_SYNCING
                }
                _uiState.update { state ->
                    state.copy(
                        syncStatus = status,
                        telemetry = state.telemetry.copy(
                            diskCacheUsedMb = usedDiskMb,
                            nextVideoPrebufferedPercent = playerManager.nextTrackPrewarmedPercent.value
                        )
                    )
                }
            }
        }
    }

    private fun maxColForRow(state: SignageUiState, row: Int): Int {
        return when (row) {
            0 -> 4 // 5 minimalist top icon actions (0..4)
            1 -> 2 // 3 hero video stage quick actions (0: Fullscreen, 1: Prev, 2: Next)
            2 -> (state.lanes.getOrNull(0)?.items?.lastIndex ?: 3)
            3 -> (state.lanes.getOrNull(1)?.items?.lastIndex ?: 3)
            else -> 0
        }
    }

    // --- Pure 4-Direction Navigation (UP, DOWN, LEFT, RIGHT) ---

    fun onDpadLeft() {
        val current = _uiState.value
        if (current.isModalOpen) {
            _uiState.update { it.copy(focusedModalIndex = (it.focusedModalIndex - 1).coerceAtLeast(0)) }
            return
        }
        if (current.screenMode == KioskScreenMode.FULLSCREEN_PLAYBACK) {
            if (current.isFullscreenHudVisible) {
                _uiState.update {
                    it.copy(fullscreenHudIndex = (it.fullscreenHudIndex - 1).coerceAtLeast(0))
                }
            } else {
                // Direct instant 0ms switch to previous video when HUD is hidden
                playerManager.stepToPreviousTrack()
            }
            return
        }
        _uiState.update { state ->
            val maxCol = maxColForRow(state, state.focusRow)
            val nextCol = if (state.focusCol > 0) state.focusCol - 1 else maxCol
            state.copy(focusCol = nextCol)
        }
    }

    fun onDpadRight() {
        val current = _uiState.value
        if (current.isModalOpen) {
            _uiState.update { it.copy(focusedModalIndex = (it.focusedModalIndex + 1).coerceAtMost(3)) }
            return
        }
        if (current.screenMode == KioskScreenMode.FULLSCREEN_PLAYBACK) {
            if (current.isFullscreenHudVisible) {
                _uiState.update {
                    it.copy(fullscreenHudIndex = (it.fullscreenHudIndex + 1).coerceAtMost(4))
                }
            } else {
                // Direct instant 0ms switch to next video when HUD is hidden
                playerManager.stepToNextTrack()
            }
            return
        }
        _uiState.update { state ->
            val maxCol = maxColForRow(state, state.focusRow)
            val nextCol = if (state.focusCol < maxCol) state.focusCol + 1 else 0
            state.copy(focusCol = nextCol)
        }
    }

    fun onDpadUp() {
        val current = _uiState.value
        if (current.isModalOpen) {
            _uiState.update { it.copy(focusedModalIndex = (it.focusedModalIndex - 2).coerceAtLeast(0)) }
            return
        }
        if (current.screenMode == KioskScreenMode.FULLSCREEN_PLAYBACK) {
            if (!current.isFullscreenHudVisible) {
                // Pressing UP in Fullscreen reveals the minimalist icon bar
                _uiState.update { it.copy(isFullscreenHudVisible = true, fullscreenHudIndex = 0) }
            } else {
                // Pressing UP again when HUD is already open exits Fullscreen back to Grid
                exitFullscreenPlayback()
            }
            return
        }
        _uiState.update { state ->
            val targetRow = (state.focusRow - 1).coerceAtLeast(0)
            val maxCol = maxColForRow(state, targetRow)
            state.copy(
                focusRow = targetRow,
                focusCol = state.focusCol.coerceAtMost(maxCol)
            )
        }
    }

    fun onDpadDown() {
        val current = _uiState.value
        if (current.isModalOpen) {
            _uiState.update { it.copy(focusedModalIndex = (it.focusedModalIndex + 2).coerceAtMost(3)) }
            return
        }
        if (current.screenMode == KioskScreenMode.FULLSCREEN_PLAYBACK) {
            if (current.isFullscreenHudVisible) {
                // Pressing DOWN hides the Fullscreen icon bar for pure unobstructed video
                _uiState.update { it.copy(isFullscreenHudVisible = false) }
            } else {
                // Pressing DOWN when HUD is hidden reveals the HUD focused on Play/Pause
                _uiState.update { it.copy(isFullscreenHudVisible = true, fullscreenHudIndex = 2) }
            }
            return
        }
        _uiState.update { state ->
            val targetRow = (state.focusRow + 1).coerceAtMost(3)
            val maxCol = maxColForRow(state, targetRow)
            state.copy(
                focusRow = targetRow,
                focusCol = state.focusCol.coerceAtMost(maxCol)
            )
        }
    }

    fun onDpadOk() {
        val state = _uiState.value
        if (state.isModalOpen) {
            executeModalAction(state.focusedModalIndex)
            return
        }
        if (state.screenMode == KioskScreenMode.FULLSCREEN_PLAYBACK) {
            if (!state.isFullscreenHudVisible) {
                // Pressing OK on unobstructed fullscreen toggles minimal HUD
                _uiState.update { it.copy(isFullscreenHudVisible = true, fullscreenHudIndex = 2) }
            } else {
                executeFullscreenHudAction(state.fullscreenHudIndex)
            }
            return
        }

        when (state.focusRow) {
            0 -> executeTopAction(state.focusCol)
            1 -> executeHeroAction(state.focusCol)
            2, 3 -> {
                val laneIdx = state.focusRow - 2
                val lane = state.lanes.getOrNull(laneIdx) ?: return
                val item = lane.items.getOrNull(state.focusCol.coerceIn(0, lane.items.lastIndex)) ?: return
                if (state.activePlayingItem.id == item.id) {
                    enterFullscreenPlayback()
                } else {
                    selectAndPlayMediaItem(item, laneIdx, state.focusCol)
                }
            }
        }
    }

    fun executeHeroAction(colIndex: Int) {
        _uiState.update { it.copy(focusRow = 1, focusCol = colIndex.coerceIn(0, 2)) }
        when (colIndex) {
            0 -> enterFullscreenPlayback()
            1 -> playerManager.stepToPreviousTrack()
            2 -> playerManager.stepToNextTrack()
        }
    }

    fun executeFullscreenHudAction(hudIndex: Int) {
        _uiState.update { it.copy(fullscreenHudIndex = hudIndex.coerceIn(0, 4)) }
        when (hudIndex) {
            0 -> exitFullscreenPlayback()
            1 -> playerManager.stepToPreviousTrack()
            2 -> playerManager.togglePlayPause()
            3 -> playerManager.stepToNextTrack()
            4 -> onMenuKey()
        }
    }

    fun onMenuKey() {
        _uiState.update { state ->
            if (state.isModalOpen) {
                state.copy(isModalOpen = false)
            } else {
                state.copy(
                    isModalOpen = true,
                    focusedModalIndex = 0 // Default to safe Resume option
                )
            }
        }
    }

    fun onBackKey(): Boolean {
        val state = _uiState.value
        if (state.isModalOpen) {
            _uiState.update { it.copy(isModalOpen = false) }
            return true
        }
        if (state.screenMode == KioskScreenMode.FULLSCREEN_PLAYBACK) {
            if (state.isFullscreenHudVisible) {
                _uiState.update { it.copy(isFullscreenHudVisible = false) }
            } else {
                exitFullscreenPlayback()
            }
            return true
        }
        return false
    }

    fun selectAndPlayMediaItem(item: SignageMediaItem, laneIndex: Int? = null, cardIndex: Int? = null) {
        playerManager.selectTrackById(item.id)
        _uiState.update { state ->
            val resolvedLaneIdx = laneIndex ?: state.lanes.indexOfFirst { it.id == item.laneId }.coerceAtLeast(0)
            val resolvedCardIdx = cardIndex ?: state.lanes.getOrNull(resolvedLaneIdx)?.items?.indexOfFirst { it.id == item.id }?.coerceAtLeast(0) ?: 0
            state.copy(
                focusRow = resolvedLaneIdx + 2,
                focusCol = resolvedCardIdx,
                activePlayingItem = item
            )
        }
    }

    fun enterFullscreenPlayback() {
        _uiState.update {
            it.copy(
                screenMode = KioskScreenMode.FULLSCREEN_PLAYBACK,
                isFullscreenHudVisible = false
            )
        }
    }

    fun exitFullscreenPlayback() {
        _uiState.update {
            it.copy(
                screenMode = KioskScreenMode.DASHBOARD_10FT,
                isFullscreenHudVisible = false
            )
        }
    }

    fun executeTopAction(actionIndex: Int) {
        _uiState.update { it.copy(focusRow = 0, focusCol = actionIndex.coerceIn(0, 4)) }
        when (actionIndex) {
            0 -> enterFullscreenPlayback()
            1 -> playerManager.togglePlayPause()
            2 -> {
                playerManager.prewarmPlaylistSegmentsInBackground()
                _uiState.update { it.copy(syncStatus = KioskSyncStatus.LIVE_HEALTHY) }
            }
            3 -> {
                _uiState.update { state ->
                    val nextMode = if (state.transitionMode == TransitionEngineMode.GAPLESS_QUEUE) {
                        TransitionEngineMode.INSTANT_FRAME_HOLD
                    } else {
                        TransitionEngineMode.GAPLESS_QUEUE
                    }
                    state.copy(transitionMode = nextMode)
                }
            }
            4 -> onMenuKey()
        }
    }

    fun executeModalAction(modalButtonIndex: Int) {
        when (modalButtonIndex) {
            0 -> {
                _uiState.update { it.copy(isModalOpen = false) }
            }
            1 -> {
                _uiState.update { state ->
                    val enabled = !state.showOverscanGuide
                    state.copy(
                        showOverscanGuide = enabled,
                        telemetry = state.telemetry.copy(overscanSafeZoneEnabled = enabled),
                        isModalOpen = false
                    )
                }
            }
            2 -> {
                playerManager.purgeAndRebuildCache()
                _uiState.update {
                    it.copy(
                        isModalOpen = false,
                        syncStatus = KioskSyncStatus.DEGRADED_SYNCING
                    )
                }
            }
            3 -> {
                val firstItem = SignageCatalogRepository.allItems.first()
                playerManager.selectTrackById(firstItem.id)
                _uiState.update {
                    it.copy(
                        isModalOpen = false,
                        focusRow = 2,
                        focusCol = 0,
                        syncStatus = KioskSyncStatus.LIVE_HEALTHY
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}

package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Android6HardwareTelemetry
import com.example.model.FocusZone
import com.example.model.KioskScreenMode
import com.example.model.KioskSyncStatus
import com.example.model.SignageCatalogRepository
import com.example.model.SignageLane
import com.example.model.SignageMediaItem
import com.example.model.TransitionEngineMode
import com.example.player.GaplessSignagePlayerManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SignageUiState(
    val lanes: List<SignageLane> = SignageCatalogRepository.lanes,
    val allItems: List<SignageMediaItem> = SignageCatalogRepository.allItems,
    val activePlayingItem: SignageMediaItem = SignageCatalogRepository.allItems.first(),
    val nextUpItem: SignageMediaItem = SignageCatalogRepository.allItems[1],
    val screenMode: KioskScreenMode = KioskScreenMode.DASHBOARD_10FT,
    val syncStatus: KioskSyncStatus = KioskSyncStatus.LIVE_HEALTHY,
    val transitionMode: TransitionEngineMode = TransitionEngineMode.GAPLESS_QUEUE,
    val telemetry: Android6HardwareTelemetry = Android6HardwareTelemetry(),
    val focusZone: FocusZone = FocusZone.PLAYLIST_LANES,
    val focusedTopActionIndex: Int = 0, // 0: Launch Fullscreen, 1: Pre-Warm Cache, 2: Transition Mode, 3: Kiosk Options
    val focusedLaneIndex: Int = 0,
    val focusedCardIndex: Int = 0,
    val isModalOpen: Boolean = false,
    val focusedModalIndex: Int = 0, // Always defaults to 0 (Non-destructive Resume) per spec!
    val showOverscanGuide: Boolean = false,
    val liveClockText: String = "10:47:08 UTC",
    val statusBannerMessage: String = "Android 6.0 Hardware SurfaceView & 150MB Ring Cache Active • 0ms Track Switch"
) {
    val focusedMediaItem: SignageMediaItem
        get() {
            val lane = lanes.getOrElse(focusedLaneIndex) { lanes.first() }
            return lane.items.getOrElse(focusedCardIndex.coerceIn(0, lane.items.lastIndex)) {
                lane.items.first()
            }
        }
}

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
                    nextUpItem = all[nextIdx],
                    statusBannerMessage = "Seamless VSYNC transition -> ${newItem.title} (0ms gap, 0 frames dropped)"
                )
            }
        }
    )

    init {
        startClockTicker()
        observePlayerTelemetry()
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("HH:mm:ss 'LOCAL'", Locale.US)
            while (isActive) {
                _uiState.update { it.copy(liveClockText = sdf.format(Date())) }
                delay(1_000L)
            }
        }
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

    // --- 2D Coordinate Matrix D-Pad Navigation ---

    fun onDpadLeft() {
        _uiState.update { state ->
            if (state.isModalOpen) {
                val nextIdx = (state.focusedModalIndex - 1).coerceAtLeast(0)
                return@update state.copy(focusedModalIndex = nextIdx)
            }
            if (state.screenMode == KioskScreenMode.FULLSCREEN_PLAYBACK) {
                playerManager.stepToPreviousTrack()
                return@update state
            }
            when (state.focusZone) {
                FocusZone.TOP_ACTIONS -> state.copy(
                    focusedTopActionIndex = (state.focusedTopActionIndex - 1).coerceAtLeast(0)
                )
                FocusZone.PLAYLIST_LANES -> state.copy(
                    focusedCardIndex = (state.focusedCardIndex - 1).coerceAtLeast(0)
                )
                FocusZone.MODAL_DIALOG -> state
            }
        }
    }

    fun onDpadRight() {
        _uiState.update { state ->
            if (state.isModalOpen) {
                val nextIdx = (state.focusedModalIndex + 1).coerceAtMost(3)
                return@update state.copy(focusedModalIndex = nextIdx)
            }
            if (state.screenMode == KioskScreenMode.FULLSCREEN_PLAYBACK) {
                playerManager.stepToNextTrack()
                return@update state
            }
            when (state.focusZone) {
                FocusZone.TOP_ACTIONS -> state.copy(
                    focusedTopActionIndex = (state.focusedTopActionIndex + 1).coerceAtMost(3)
                )
                FocusZone.PLAYLIST_LANES -> {
                    val laneMax = state.lanes.getOrNull(state.focusedLaneIndex)?.items?.lastIndex ?: 0
                    state.copy(
                        focusedCardIndex = (state.focusedCardIndex + 1).coerceAtMost(laneMax)
                    )
                }
                FocusZone.MODAL_DIALOG -> state
            }
        }
    }

    fun onDpadUp() {
        _uiState.update { state ->
            if (state.isModalOpen) {
                val nextIdx = (state.focusedModalIndex - 1).coerceAtLeast(0)
                return@update state.copy(focusedModalIndex = nextIdx)
            }
            if (state.screenMode == KioskScreenMode.FULLSCREEN_PLAYBACK) {
                return@update state
            }
            when (state.focusZone) {
                FocusZone.TOP_ACTIONS -> state
                FocusZone.PLAYLIST_LANES -> {
                    if (state.focusedLaneIndex > 0) {
                        val prevLaneIdx = state.focusedLaneIndex - 1
                        val prevLaneMax = state.lanes[prevLaneIdx].items.lastIndex
                        state.copy(
                            focusedLaneIndex = prevLaneIdx,
                            focusedCardIndex = state.focusedCardIndex.coerceAtMost(prevLaneMax)
                        )
                    } else {
                        state.copy(
                            focusZone = FocusZone.TOP_ACTIONS,
                            focusedTopActionIndex = state.focusedCardIndex.coerceIn(0, 3)
                        )
                    }
                }
                FocusZone.MODAL_DIALOG -> state
            }
        }
    }

    fun onDpadDown() {
        _uiState.update { state ->
            if (state.isModalOpen) {
                val nextIdx = (state.focusedModalIndex + 1).coerceAtMost(3)
                return@update state.copy(focusedModalIndex = nextIdx)
            }
            if (state.screenMode == KioskScreenMode.FULLSCREEN_PLAYBACK) {
                return@update state
            }
            when (state.focusZone) {
                FocusZone.TOP_ACTIONS -> {
                    val firstLaneMax = state.lanes.firstOrNull()?.items?.lastIndex ?: 0
                    state.copy(
                        focusZone = FocusZone.PLAYLIST_LANES,
                        focusedLaneIndex = 0,
                        focusedCardIndex = state.focusedTopActionIndex.coerceAtMost(firstLaneMax)
                    )
                }
                FocusZone.PLAYLIST_LANES -> {
                    if (state.focusedLaneIndex < state.lanes.lastIndex) {
                        val nextLaneIdx = state.focusedLaneIndex + 1
                        val nextLaneMax = state.lanes[nextLaneIdx].items.lastIndex
                        state.copy(
                            focusedLaneIndex = nextLaneIdx,
                            focusedCardIndex = state.focusedCardIndex.coerceAtMost(nextLaneMax)
                        )
                    } else {
                        state
                    }
                }
                FocusZone.MODAL_DIALOG -> state
            }
        }
    }

    fun onDpadOk() {
        val state = _uiState.value
        if (state.isModalOpen) {
            executeModalAction(state.focusedModalIndex)
            return
        }
        if (state.screenMode == KioskScreenMode.FULLSCREEN_PLAYBACK) {
            playerManager.togglePlayPause()
            return
        }
        when (state.focusZone) {
            FocusZone.TOP_ACTIONS -> executeTopAction(state.focusedTopActionIndex)
            FocusZone.PLAYLIST_LANES -> {
                val targetItem = state.focusedMediaItem
                if (state.activePlayingItem.id == targetItem.id) {
                    // Pressing OK on already playing item enters Fullscreen 1080p Kiosk Video Mode
                    enterFullscreenPlayback()
                } else {
                    selectAndPlayMediaItem(targetItem)
                }
            }
            FocusZone.MODAL_DIALOG -> executeModalAction(state.focusedModalIndex)
        }
    }

    fun onMenuKey() {
        _uiState.update { state ->
            if (state.isModalOpen) {
                state.copy(isModalOpen = false, focusZone = FocusZone.PLAYLIST_LANES)
            } else {
                // Spec requirement: "All actions inside modals default D-pad focus to the non-destructive action"
                state.copy(
                    isModalOpen = true,
                    focusZone = FocusZone.MODAL_DIALOG,
                    focusedModalIndex = 0
                )
            }
        }
    }

    fun onBackKey(): Boolean {
        val state = _uiState.value
        if (state.isModalOpen) {
            _uiState.update { it.copy(isModalOpen = false, focusZone = FocusZone.PLAYLIST_LANES) }
            return true
        }
        if (state.screenMode == KioskScreenMode.FULLSCREEN_PLAYBACK) {
            exitFullscreenPlayback()
            return true
        }
        return false
    }

    // --- Direct Actions & Card Selection ---

    fun selectAndPlayMediaItem(item: SignageMediaItem, laneIndex: Int? = null, cardIndex: Int? = null) {
        playerManager.selectTrackById(item.id)
        _uiState.update { state ->
            val resolvedLaneIdx = laneIndex ?: state.lanes.indexOfFirst { it.id == item.laneId }.coerceAtLeast(0)
            val resolvedCardIdx = cardIndex ?: state.lanes.getOrNull(resolvedLaneIdx)?.items?.indexOfFirst { it.id == item.id }?.coerceAtLeast(0) ?: 0
            state.copy(
                focusZone = FocusZone.PLAYLIST_LANES,
                focusedLaneIndex = resolvedLaneIdx,
                focusedCardIndex = resolvedCardIdx,
                activePlayingItem = item
            )
        }
    }

    fun enterFullscreenPlayback() {
        _uiState.update {
            it.copy(
                screenMode = KioskScreenMode.FULLSCREEN_PLAYBACK,
                statusBannerMessage = "Fullscreen 1080p Kiosk Loop Active • Gapless Ring Buffer Locked"
            )
        }
    }

    fun exitFullscreenPlayback() {
        _uiState.update {
            it.copy(
                screenMode = KioskScreenMode.DASHBOARD_10FT,
                statusBannerMessage = "Returned to 10-Foot Command Dashboard • Background Playback Uninterrupted"
            )
        }
    }

    fun executeTopAction(actionIndex: Int) {
        _uiState.update { it.copy(focusZone = FocusZone.TOP_ACTIONS, focusedTopActionIndex = actionIndex) }
        when (actionIndex) {
            0 -> enterFullscreenPlayback()
            1 -> {
                playerManager.prewarmPlaylistSegmentsInBackground()
                _uiState.update {
                    it.copy(
                        syncStatus = KioskSyncStatus.LIVE_HEALTHY,
                        statusBannerMessage = "Pre-warmed all 8 playlist headers (768KB GOP keyframes) into Local Flash Cache"
                    )
                }
            }
            2 -> {
                _uiState.update { state ->
                    val nextMode = if (state.transitionMode == TransitionEngineMode.GAPLESS_QUEUE) {
                        TransitionEngineMode.INSTANT_FRAME_HOLD
                    } else {
                        TransitionEngineMode.GAPLESS_QUEUE
                    }
                    state.copy(
                        transitionMode = nextMode,
                        statusBannerMessage = "Transition Engine switched to: ${nextMode.title}"
                    )
                }
            }
            3 -> onMenuKey()
        }
    }

    fun executeModalAction(modalButtonIndex: Int) {
        when (modalButtonIndex) {
            0 -> {
                // Non-destructive default action: Resume Kiosk Playback
                _uiState.update {
                    it.copy(
                        isModalOpen = false,
                        focusZone = FocusZone.PLAYLIST_LANES,
                        statusBannerMessage = "Kiosk Override closed • Playback continuing seamlessly"
                    )
                }
            }
            1 -> {
                // Non-destructive utility: Toggle Overscan Safe Zone Border Guide
                _uiState.update { state ->
                    val enabled = !state.showOverscanGuide
                    state.copy(
                        showOverscanGuide = enabled,
                        telemetry = state.telemetry.copy(overscanSafeZoneEnabled = enabled),
                        isModalOpen = false,
                        focusZone = FocusZone.PLAYLIST_LANES,
                        statusBannerMessage = if (enabled) {
                            "Title-Safe Overscan Guide (4rem / 64px) ENABLED"
                        } else {
                            "Title-Safe Overscan Guide DISABLED"
                        }
                    )
                }
            }
            2 -> {
                // Destructive action 1: Purge & Rebuild Local Flash Cache
                playerManager.purgeAndRebuildCache()
                _uiState.update {
                    it.copy(
                        isModalOpen = false,
                        focusZone = FocusZone.PLAYLIST_LANES,
                        syncStatus = KioskSyncStatus.DEGRADED_SYNCING,
                        statusBannerMessage = "Cache Purged • Re-warming 150MB Android 6 Flash Ring Buffer..."
                    )
                }
            }
            3 -> {
                // Destructive action 2: Restart Kiosk Node Sequence
                val firstItem = SignageCatalogRepository.allItems.first()
                playerManager.selectTrackById(firstItem.id)
                _uiState.update {
                    it.copy(
                        isModalOpen = false,
                        focusZone = FocusZone.PLAYLIST_LANES,
                        focusedLaneIndex = 0,
                        focusedCardIndex = 0,
                        syncStatus = KioskSyncStatus.LIVE_HEALTHY,
                        statusBannerMessage = "Kiosk Node 04-A Sequence Reset to Track #01 (Zero Surface Teardown)"
                    )
                }
            }
        }
    }

    fun focusModalOption(index: Int) {
        _uiState.update { it.copy(focusedModalIndex = index.coerceIn(0, 3)) }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}

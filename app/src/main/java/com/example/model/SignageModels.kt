package com.example.model

import androidx.compose.runtime.Immutable

enum class KioskSyncStatus(
    val label: String,
    val detail: String
) {
    LIVE_HEALTHY("LIVE STREAM SYNC", "All playlist segments verified in local ring cache"),
    DEGRADED_SYNCING("PRE-WARMING CACHE", "Pre-fetching upcoming 1080p segment into flash pool"),
    OFFLINE_STALLED("OFFLINE FAILSAFE", "Running autonomous loop from persistent local storage")
}

enum class KioskScreenMode {
    DASHBOARD_10FT,
    FULLSCREEN_PLAYBACK
}

enum class FocusZone {
    TOP_ACTIONS,
    PLAYLIST_LANES,
    MODAL_DIALOG
}

enum class TransitionEngineMode(
    val title: String,
    val description: String
) {
    GAPLESS_QUEUE(
        "Native Gapless Concatenation",
        "Pre-queues entire schedule in MediaCodec pipeline; 0ms switch without Surface teardown"
    ),
    INSTANT_FRAME_HOLD(
        "Zero-Black-Frame Poster Hold",
        "Locks last decoded frame on SurfaceView until next stream fires onRenderedFirstFrame"
    )
}

@Immutable
data class SignageMediaItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val laneId: String,
    val laneTitle: String,
    val durationSec: Int,
    val durationFormatted: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val bitrateMbps: String,
    val codecInfo: String,
    val scheduleSlot: String,
    val zoneName: String,
    val accentHex: Long,
    val preCachePriority: Int
)

@Immutable
data class SignageLane(
    val id: String,
    val title: String,
    val badgeText: String,
    val items: List<SignageMediaItem>
)

@Immutable
data class Android6HardwareTelemetry(
    val socName: String = "Legacy TV SoC (Android 6.0 / API 23 Profile)",
    val decoderName: String = "OMX.hw.video_decoder.avc (SurfaceView Direct)",
    val ramBufferUsedMb: Int = 11,
    val ramBufferCapMb: Int = 16,
    val diskCacheUsedMb: Int = 68,
    val diskCacheCapMb: Int = 150,
    val nextVideoPrebufferedPercent: Int = 100,
    val droppedFrames: Int = 0,
    val switchLatencyMs: Int = 0,
    val zeroBlurGpuMode: Boolean = true,
    val overscanSafeZoneEnabled: Boolean = false
)

object SignageCatalogRepository {

    // Hotlinked high-availability 16:9 architectural, showroom, and retail imagery + fast CDN MP4 streams
    val allItems: List<SignageMediaItem> = listOf(
        // Lane 1: Showroom Primary Loop (Active Kiosk Rotation)
        SignageMediaItem(
            id = "node_01",
            title = "Apex Flagship Atrium — Architectural Light Study",
            subtitle = "Main Concourse North Wall • Seamless Loop #01",
            laneId = "lane_showroom",
            laneTitle = "01 // SHOWROOM PRIMARY ROTATION (GAPLESS LOOP)",
            durationSec = 15,
            durationFormatted = "00:15",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=1280&q=80",
            bitrateMbps = "3.8 Mbps",
            codecInfo = "H.264 Main@L4.0 • 1080p30",
            scheduleSlot = "08:00 - 22:00",
            zoneName = "ATRIUM WEST",
            accentHex = 0xFF966A84,
            preCachePriority = 1
        ),
        SignageMediaItem(
            id = "node_02",
            title = "Monolith Retail Gallery — Autumn Materiality Reel",
            subtitle = "Flagship Display Cluster • Seamless Loop #02",
            laneId = "lane_showroom",
            laneTitle = "01 // SHOWROOM PRIMARY ROTATION (GAPLESS LOOP)",
            durationSec = 15,
            durationFormatted = "00:15",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=1280&q=80",
            bitrateMbps = "4.1 Mbps",
            codecInfo = "H.264 Main@L4.0 • 1080p30",
            scheduleSlot = "08:00 - 22:00",
            zoneName = "GALLERY 02",
            accentHex = 0xFF866E98,
            preCachePriority = 1
        ),
        SignageMediaItem(
            id = "node_03",
            title = "Kinetic Mobility Pavilion — Aerodynamic Telemetry",
            subtitle = "Showroom Turntable Stage • Seamless Loop #03",
            laneId = "lane_showroom",
            laneTitle = "01 // SHOWROOM PRIMARY ROTATION (GAPLESS LOOP)",
            durationSec = 15,
            durationFormatted = "00:15",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=1280&q=80",
            bitrateMbps = "3.5 Mbps",
            codecInfo = "H.264 Base@L3.1 • 720p60",
            scheduleSlot = "08:00 - 22:00",
            zoneName = "TURNTABLE A",
            accentHex = 0xFFB2839E,
            preCachePriority = 1
        ),
        SignageMediaItem(
            id = "node_04",
            title = "Nordic Acoustic Lounge — Spatial Audio Showcase",
            subtitle = "Listening Suite Kiosk • Seamless Loop #04",
            laneId = "lane_showroom",
            laneTitle = "01 // SHOWROOM PRIMARY ROTATION (GAPLESS LOOP)",
            durationSec = 15,
            durationFormatted = "00:15",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoy.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1280&q=80",
            bitrateMbps = "3.2 Mbps",
            codecInfo = "H.264 Main@L4.0 • 1080p30",
            scheduleSlot = "09:00 - 21:00",
            zoneName = "SUITE 04",
            accentHex = 0xFF8A6F7E,
            preCachePriority = 2
        ),

        // Lane 2: Scheduled Retail & Wayfinding Promotions
        SignageMediaItem(
            id = "node_05",
            title = "Executive Skybridge — Wayfinding & Transit Matrix",
            subtitle = "Elevator Bank B • High-Contrast Telemetry Board",
            laneId = "lane_scheduled",
            laneTitle = "02 // SCHEDULED WAYFINDING & RETAIL CANVASES",
            durationSec = 60,
            durationFormatted = "01:00",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=1280&q=80",
            bitrateMbps = "2.9 Mbps",
            codecInfo = "H.264 Base@L3.1 • 1080p30",
            scheduleSlot = "06:00 - 23:59",
            zoneName = "SKYBRIDGE",
            accentHex = 0xFF866E98,
            preCachePriority = 2
        ),
        SignageMediaItem(
            id = "node_06",
            title = "Horology Atelier — Precision Chronograph Macro",
            subtitle = "Luxury Vitrine Display • Anti-Burn-In Tonal Field",
            laneId = "lane_scheduled",
            laneTitle = "02 // SCHEDULED WAYFINDING & RETAIL CANVASES",
            durationSec = 45,
            durationFormatted = "00:45",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=1280&q=80",
            bitrateMbps = "3.6 Mbps",
            codecInfo = "H.264 Main@L4.0 • 1080p30",
            scheduleSlot = "10:00 - 20:00",
            zoneName = "VITRINE 01",
            accentHex = 0xFF966A84,
            preCachePriority = 2
        ),
        SignageMediaItem(
            id = "node_07",
            title = "Culinary Conservatory — Evening Tasting Menu",
            subtitle = "Mezzanine Hospitality Wall • Scheduled 17:00 Trigger",
            laneId = "lane_scheduled",
            laneTitle = "02 // SCHEDULED WAYFINDING & RETAIL CANVASES",
            durationSec = 30,
            durationFormatted = "00:30",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackSeeTheWorld.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=1280&q=80",
            bitrateMbps = "3.4 Mbps",
            codecInfo = "H.264 Main@L4.0 • 1080p30",
            scheduleSlot = "17:00 - 23:00",
            zoneName = "MEZZANINE",
            accentHex = 0xFFF59E0B,
            preCachePriority = 3
        ),
        SignageMediaItem(
            id = "node_08",
            title = "Control Room Operations — Network Node Diagnostics",
            subtitle = "NOC Wall Panel 04 • Low-Fill-Rate Safe Palette",
            laneId = "lane_scheduled",
            laneTitle = "02 // SCHEDULED WAYFINDING & RETAIL CANVASES",
            durationSec = 90,
            durationFormatted = "01:30",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=1280&q=80",
            bitrateMbps = "2.8 Mbps",
            codecInfo = "H.264 Base@L3.1 • 720p30",
            scheduleSlot = "24/7 CONTINUOUS",
            zoneName = "NOC WALL 04",
            accentHex = 0xFF8A6F7E,
            preCachePriority = 3
        )
    )

    val lanes: List<SignageLane> = allItems
        .groupBy { it.laneId }
        .map { (laneId, items) ->
            SignageLane(
                id = laneId,
                title = items.first().laneTitle,
                badgeText = if (laneId == "lane_showroom") "GAPLESS RING BUFFER ACTIVE" else "PRE-CACHED IN FLASH",
                items = items
            )
        }
}

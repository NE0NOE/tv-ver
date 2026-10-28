package com.example.ui.components

import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.model.SignageMediaItem
import com.example.model.TransitionEngineMode
import com.example.player.GaplessSignagePlayerManager
import com.example.ui.theme.ApexInactiveTileBorder
import com.example.ui.theme.ApexPrimaryMauve
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexSurfaceContainerLow
import com.example.ui.theme.ApexTertiaryLive
import com.example.ui.theme.ApexTextPrimary

/**
 * Ultra-Minimalist Hardware-Accelerated Gapless Video Stage for Android 6.0 TV:
 * - Zero text clutter, zero control overlays, zero loading screen between videos.
 * - Uses PlayerView with `setKeepContentOnPlayerReset(true)` and transparent shutter.
 */
@OptIn(UnstableApi::class)
@Composable
fun GaplessVideoStage(
    playerManager: GaplessSignagePlayerManager,
    activeItem: SignageMediaItem,
    nextUpItem: SignageMediaItem,
    transitionMode: TransitionEngineMode,
    isFullscreen: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val nextPrewarmedPct by playerManager.nextTrackPrewarmedPercent.collectAsState()
    val positionSec by playerManager.currentPositionSec.collectAsState()
    val durationSec by playerManager.currentDurationSec.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Box(
        modifier = modifier
            .background(ApexSurfaceContainerLow)
            .testTag("gapless_video_stage")
    ) {
        // 1. Pre-Decoded Frame Shield (Prevents any black flash or loading screen between videos)
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(activeItem.thumbnailUrl)
                .crossfade(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = activeItem.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle animated vertical pulse line for offline/fallback visual continuity
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scanX = size.width * sweepProgress
            drawLine(
                color = Color(activeItem.accentHex).copy(alpha = 0.35f),
                start = Offset(scanX, 0f),
                end = Offset(scanX, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }

        // 2. Hidden 1dp Pre-Decoder warming the next video's frame in RAM
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(nextUpItem.thumbnailUrl)
                .crossfade(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = null,
            modifier = Modifier.size(1.dp)
        )

        // 3. Hardware SurfaceView with Shutter Disabled & Last-Frame Hold Enabled
        if (playerManager.exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setKeepContentOnPlayerReset(true)
                        setShutterBackgroundColor(AndroidColor.TRANSPARENT)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        player = playerManager.exoPlayer
                    }
                },
                update = { view ->
                    if (view.player !== playerManager.exoPlayer) {
                        view.player = playerManager.exoPlayer
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 4. Minimalist Live Indicator Dot (Top-Left, no text)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActivePlaybackPill()
        }

        // 5. Minimalist Compact Timestamp Pill (Bottom-Right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(ApexSurface.copy(alpha = 0.82f))
                .border(1.dp, ApexInactiveTileBorder.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            val posFormatted = String.format("%02d:%02d", positionSec / 60, positionSec % 60)
            Text(
                text = posFormatted,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = ApexTextPrimary
            )
        }

        // 6. Ultra-Thin 3dp Bottom Edge Progress & Pre-Buffer Line
        val progressFraction = (positionSec.toFloat() / durationSec.coerceAtLeast(1).toFloat())
            .coerceIn(0.04f, 1f)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(3.dp)
                .background(ApexSurfaceContainerLow.copy(alpha = 0.6f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((nextPrewarmedPct / 100f).coerceIn(0.85f, 1f))
                    .fillMaxHeight()
                    .background(ApexTertiaryLive.copy(alpha = 0.4f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction)
                    .fillMaxHeight()
                    .background(ApexPrimaryMauve)
            )
        }
    }
}

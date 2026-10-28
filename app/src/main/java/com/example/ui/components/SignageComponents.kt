package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.model.KioskSyncStatus
import com.example.model.SignageMediaItem
import com.example.ui.theme.ApexInactiveTileBorder
import com.example.ui.theme.ApexModalScrim
import com.example.ui.theme.ApexOfflineRed
import com.example.ui.theme.ApexOfflineRedTint
import com.example.ui.theme.ApexPrimaryHighLum
import com.example.ui.theme.ApexPrimaryMauve
import com.example.ui.theme.ApexPrimaryMauveHalo
import com.example.ui.theme.ApexSecondaryContainer
import com.example.ui.theme.ApexSecondaryRoseSlate
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexSurfaceContainer
import com.example.ui.theme.ApexSurfaceContainerHigh
import com.example.ui.theme.ApexSurfaceContainerHighest
import com.example.ui.theme.ApexSurfaceContainerLow
import com.example.ui.theme.ApexTertiaryLight
import com.example.ui.theme.ApexTertiaryLive
import com.example.ui.theme.ApexTertiaryLiveTint
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexWarningAmber
import com.example.ui.theme.ApexWarningAmberTint

/**
 * Level 3 Focused / D-Pad Engaged State:
 * - Zero Gaussian blur / zero drop-shadow for Android 6 GPU efficiency
 * - Scale transform (1.05x for cards, 1.06x for icon buttons)
 * - Pre-baked 8dp outer halo: rgba(150, 106, 132, 0.45)
 * - Crisp 3dp solid #966A84 ring with 1dp inset separation stroke
 */
@Composable
fun Modifier.apexDpadFocusRing(
    isFocused: Boolean,
    scaleFactor: Float = 1.05f,
    cornerRadius: Dp = 24.dp
): Modifier {
    val animatedScale by animateFloatAsState(
        targetValue = if (isFocused) scaleFactor else 1.0f,
        animationSpec = tween(durationMillis = 130),
        label = "dpadScale"
    )
    return this
        .scale(animatedScale)
        .drawBehind {
            if (isFocused) {
                val radiusPx = cornerRadius.toPx()
                val haloStrokePx = 8.dp.toPx()
                val primaryRingPx = 3.dp.toPx()
                val insetStrokePx = 1.dp.toPx()

                // 1. Pre-baked 8dp outer halo
                drawRoundRect(
                    color = ApexPrimaryMauveHalo,
                    topLeft = Offset(-haloStrokePx / 2f, -haloStrokePx / 2f),
                    size = Size(size.width + haloStrokePx, size.height + haloStrokePx),
                    cornerRadius = CornerRadius(radiusPx + haloStrokePx / 2f),
                    style = Stroke(width = haloStrokePx)
                )

                // 2. Crisp 3dp solid #966A84 ring
                drawRoundRect(
                    color = ApexPrimaryMauve,
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = CornerRadius(radiusPx),
                    style = Stroke(width = primaryRingPx)
                )

                // 3. 1dp inset separation stroke
                drawRoundRect(
                    color = ApexSurface,
                    topLeft = Offset(primaryRingPx, primaryRingPx),
                    size = Size(
                        (size.width - primaryRingPx * 2).coerceAtLeast(0f),
                        (size.height - primaryRingPx * 2).coerceAtLeast(0f)
                    ),
                    cornerRadius = CornerRadius((radiusPx - primaryRingPx).coerceAtLeast(0f)),
                    style = Stroke(width = insetStrokePx)
                )
            }
        }
}

/**
 * Minimalist Status Dot Pill (no text or single short code)
 */
@Composable
fun ApexStatusBadge(
    status: KioskSyncStatus,
    modifier: Modifier = Modifier
) {
    val (bgTint, strokeColor) = when (status) {
        KioskSyncStatus.LIVE_HEALTHY -> Pair(ApexTertiaryLiveTint, ApexTertiaryLive)
        KioskSyncStatus.DEGRADED_SYNCING -> Pair(ApexWarningAmberTint, ApexWarningAmber)
        KioskSyncStatus.OFFLINE_STALLED -> Pair(ApexOfflineRedTint, ApexOfflineRed)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgTint)
            .border(1.5.dp, strokeColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(strokeColor)
        )
    }
}

/**
 * Minimalist D-Pad Navigable Icon Button (Almost zero text, high-contrast iconography)
 */
@Composable
fun ApexMinimalIconButton(
    icon: ImageVector,
    contentDescription: String,
    isFocused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shortLabel: String? = null,
    isActiveAccent: Boolean = false,
    isDestructive: Boolean = false,
    testTag: String = "minimal_icon_button"
) {
    val containerColor = when {
        isFocused && isDestructive -> ApexOfflineRed
        isFocused -> ApexPrimaryMauve
        isActiveAccent -> ApexSecondaryContainer
        else -> ApexSurfaceContainerHigh
    }

    val borderColor = when {
        isFocused -> ApexPrimaryHighLum
        isDestructive -> ApexOfflineRed.copy(alpha = 0.7f)
        isActiveAccent -> ApexTertiaryLive
        else -> ApexInactiveTileBorder
    }

    val iconTint = when {
        isFocused -> ApexTextPrimary
        isDestructive -> Color(0xFFFFB4AB)
        isActiveAccent -> ApexTertiaryLight
        else -> ApexTextPrimary
    }

    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .apexDpadFocusRing(
                isFocused = isFocused,
                scaleFactor = 1.06f,
                cornerRadius = 999.dp
            )
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        border = BorderStroke(if (isFocused) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (shortLabel != null) 18.dp else 14.dp,
                vertical = 12.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            if (!shortLabel.isNullOrEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = shortLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = iconTint
                )
            }
        }
    }
}

/**
 * Ultra-Minimalist Focusable Media Card (Pure 16:9 visual tile, almost zero text):
 * - Resting: 1px #8A6F7E border, rounded 24dp corners, 16:9 hotlinked thumbnail, bottom-right duration pill.
 * - Focused (D-pad UP/DOWN/LEFT/RIGHT): scale(1.05), 3px solid #966A84 ring + 8px outer halo.
 * - Active / Playing: 4px left border accent in #866E98 + animated equalizer pill.
 */
@Composable
fun ApexFocusableMediaCard(
    item: SignageMediaItem,
    isFocused: Boolean,
    isPlaying: Boolean,
    isPreCached: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cardCornerRadius = 22.dp

    val containerColor = when {
        isFocused -> ApexSurfaceContainerHighest
        isPlaying -> ApexSecondaryContainer
        else -> ApexSurfaceContainer
    }

    val restingBorder = when {
        isFocused -> BorderStroke(2.dp, ApexPrimaryMauve)
        isPlaying -> BorderStroke(1.5.dp, ApexTertiaryLive)
        else -> BorderStroke(1.dp, ApexInactiveTileBorder)
    }

    Box(
        modifier = modifier
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .aspectRatio(16f / 9f)
            .apexDpadFocusRing(
                isFocused = isFocused,
                scaleFactor = 1.05f,
                cornerRadius = cardCornerRadius
            )
            .clip(RoundedCornerShape(cardCornerRadius))
            .background(containerColor)
            .border(restingBorder, RoundedCornerShape(cardCornerRadius))
            .drawBehind {
                if (isPlaying) {
                    val accentWidth = 5.dp.toPx()
                    drawRoundRect(
                        color = ApexTertiaryLive,
                        topLeft = Offset.Zero,
                        size = Size(accentWidth, size.height),
                        cornerRadius = CornerRadius(accentWidth, accentWidth)
                    )
                }
            }
            .clickable(onClick = onClick)
            .testTag("media_card_${item.id}")
    ) {
        // Full-bleed 16:9 Hotlinked Image
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(item.thumbnailUrl)
                .crossfade(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Top-Left Pre-Cache Dot Indicator (Pure visual, no text)
        if (isPreCached) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(ApexTertiaryLive)
                    .border(1.5.dp, ApexSurface, CircleShape)
            )
        }

        // Top-Right Active Equalizer Pill (when playing)
        if (isPlaying) {
            ActivePlaybackPill(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            )
        }

        // Center Fullscreen Icon Cue when focused on the already-playing item
        if (isFocused) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ApexSurface.copy(alpha = 0.82f))
                    .border(2.dp, ApexPrimaryMauve, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Fullscreen else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Enter Fullscreen" else "Play Video",
                    tint = ApexPrimaryHighLum,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom-Right Duration Timestamp (e.g., 00:15)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ApexSurface.copy(alpha = 0.88f))
                .border(1.dp, ApexInactiveTileBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = item.durationFormatted,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = ApexTextPrimary
            )
        }
    }
}

@Composable
fun ActivePlaybackPill(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "eqBars")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(360), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "b3"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(ApexTertiaryLive)
            .border(1.dp, ApexTertiaryLight, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            modifier = Modifier.height(12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(bar1)
                    .background(ApexTextPrimary, RoundedCornerShape(1.dp))
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(bar2)
                    .background(ApexTextPrimary, RoundedCornerShape(1.dp))
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(bar3)
                    .background(ApexTextPrimary, RoundedCornerShape(1.dp))
            )
        }
    }
}

/**
 * Minimalist 2x2 Icon Grid Modal (Navigable with UP, DOWN, LEFT, RIGHT, OK)
 * - Default D-Pad focus on index 0 (Non-destructive Resume)
 */
@Composable
fun ApexKioskOverrideModal(
    focusedIndex: Int,
    overscanGuideActive: Boolean,
    onActionClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexModalScrim)
            .clickable(onClick = onDismiss)
            .testTag("kiosk_override_modal"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(380.dp)
                .clickable(enabled = false, onClick = {})
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            color = ApexSurfaceContainer,
            border = BorderStroke(2.dp, ApexPrimaryMauve)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Row 1: Safe Actions (0: Resume, 1: Overscan Frame)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ApexMinimalIconButton(
                        icon = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        shortLabel = "OK",
                        isFocused = focusedIndex == 0,
                        onClick = { onActionClick(0) },
                        modifier = Modifier.weight(1f),
                        testTag = "modal_btn_resume"
                    )
                    ApexMinimalIconButton(
                        icon = Icons.Default.CropFree,
                        contentDescription = "Overscan Safe Frame",
                        shortLabel = if (overscanGuideActive) "ON" else "OFF",
                        isActiveAccent = overscanGuideActive,
                        isFocused = focusedIndex == 1,
                        onClick = { onActionClick(1) },
                        modifier = Modifier.weight(1f),
                        testTag = "modal_btn_overscan"
                    )
                }

                // Row 2: Cache & Reset Actions (2: Purge Cache, 3: Reset Loop)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ApexMinimalIconButton(
                        icon = Icons.Default.DeleteSweep,
                        contentDescription = "Purge Cache",
                        shortLabel = "CACHE",
                        isFocused = focusedIndex == 2,
                        isDestructive = true,
                        onClick = { onActionClick(2) },
                        modifier = Modifier.weight(1f),
                        testTag = "modal_btn_purge_cache"
                    )
                    ApexMinimalIconButton(
                        icon = Icons.Default.RestartAlt,
                        contentDescription = "Reset Kiosk Loop",
                        shortLabel = "RESET",
                        isFocused = focusedIndex == 3,
                        isDestructive = true,
                        onClick = { onActionClick(3) },
                        modifier = Modifier.weight(1f),
                        testTag = "modal_btn_restart_node"
                    )
                }
            }
        }
    }
}

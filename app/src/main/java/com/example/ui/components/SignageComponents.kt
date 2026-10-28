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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.ApexTextMutedMetadata
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexWarningAmber
import com.example.ui.theme.ApexWarningAmberTint

/**
 * Modifier implementing the exact Level 3 Focused / D-Pad Engaged State from the Apex Signage Spec:
 * - No real-time Gaussian blur or expensive shadows (optimized for Android 6 SoC GPUs)
 * - Physical transform scale (`1.05f` for cards, `1.04f` for buttons)
 * - Pre-baked 8dp solid outer halo (`rgba(150, 106, 132, 0.45)`)
 * - Crisp 3dp solid `#966A84` ring with 1dp inset separation stroke
 */
@Composable
fun Modifier.apexDpadFocusRing(
    isFocused: Boolean,
    scaleFactor: Float = 1.05f,
    cornerRadius: Dp = 24.dp
): Modifier {
    val animatedScale by animateFloatAsState(
        targetValue = if (isFocused) scaleFactor else 1.0f,
        animationSpec = tween(durationMillis = 140),
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

                // 1. Pre-baked 8px solid outer halo: rgba(150, 106, 132, 0.45)
                drawRoundRect(
                    color = ApexPrimaryMauveHalo,
                    topLeft = Offset(-haloStrokePx / 2f, -haloStrokePx / 2f),
                    size = Size(size.width + haloStrokePx, size.height + haloStrokePx),
                    cornerRadius = CornerRadius(radiusPx + haloStrokePx / 2f),
                    style = Stroke(width = haloStrokePx)
                )

                // 2. Crisp 3px solid #966A84 dusty mauve ring
                drawRoundRect(
                    color = ApexPrimaryMauve,
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = CornerRadius(radiusPx),
                    style = Stroke(width = primaryRingPx)
                )

                // 3. 1px inset separation stroke
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
 * Component 4: Status Badges & Sync Chips
 * - Live / Healthy: Background alpha tint, text #866E98, 1px stroke #866E98, prepended by 8px solid circle
 * - Degraded / Syncing: Background alpha tint, text #F59E0B, 1px stroke #F59E0B
 * - Offline / Stalled: Background alpha tint, text #EF4444, 1px stroke #EF4444
 */
@Composable
fun ApexStatusBadge(
    status: KioskSyncStatus,
    modifier: Modifier = Modifier
) {
    val (bgTint, strokeColor, textColor) = when (status) {
        KioskSyncStatus.LIVE_HEALTHY -> Triple(ApexTertiaryLiveTint, ApexTertiaryLive, ApexTertiaryLight)
        KioskSyncStatus.DEGRADED_SYNCING -> Triple(ApexWarningAmberTint, ApexWarningAmber, ApexWarningAmber)
        KioskSyncStatus.OFFLINE_STALLED -> Triple(ApexOfflineRedTint, ApexOfflineRed, ApexOfflineRed)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgTint)
            .border(1.dp, strokeColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(strokeColor)
        )
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            maxLines = 1
        )
    }
}

/**
 * Component 2: Remote Action Buttons
 * - Default State: Surface fill, crisp text (Work Sans, 18sp, weight 600), interior padding 12dp 24dp
 * - Focused State: Solid #966A84 background fill, text color changes for maximum contrast, scale 1.04, outer focus border
 * - Legend Badges: Embedded inline pill icons representing TV remote keys ([OK], [BACK], [MENU])
 */
@Composable
fun ApexRemoteActionButton(
    text: String,
    legendKey: String,
    isFocused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    testTag: String = "remote_action_button"
) {
    val containerColor = when {
        isFocused && isDestructive -> ApexOfflineRed
        isFocused -> ApexPrimaryMauve
        isDestructive -> ApexSurfaceContainerHigh
        else -> ApexSurfaceContainerHigh
    }

    val borderColor = when {
        isFocused -> ApexPrimaryHighLum
        isDestructive -> ApexOfflineRed.copy(alpha = 0.7f)
        else -> ApexInactiveTileBorder
    }

    val textColor = when {
        isFocused -> ApexTextPrimary
        isDestructive -> Color(0xFFFFB4AB)
        else -> ApexTextPrimary
    }

    val badgeBgColor = if (isFocused) {
        ApexSurface.copy(alpha = 0.45f)
    } else {
        ApexSecondaryContainer
    }

    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .apexDpadFocusRing(
                isFocused = isFocused,
                scaleFactor = 1.04f,
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
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Embedded inline pill legend badge ([OK], [MENU], [CACHE], etc.)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(badgeBgColor)
                    .border(1.dp, ApexInactiveTileBorder.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "[$legendKey]",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isFocused) ApexPrimaryHighLum else ApexTextMutedMetadata
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Component 1: Interactive Focusable Cards (Playlist & Media Items)
 * - Resting (Level 1): Tonal fill (#241E20 / #2F282B), 1px border (#8A6F7E), rounded 24dp radius.
 *   Thumbnail images use a 16:9 ratio with duration timestamps anchored in the bottom-right corner.
 * - Focused (Level 3 D-pad targeting): Transforms to scale(1.05). Outline becomes 3px solid #966A84
 *   dusty mauve ring accompanied by an ambient 8px halo. Card title shifts to high-contrast font weight 700.
 * - Active / Playing (Level 2): Displays a persistent 3px left border accent in #866E98 along with
 *   an animated playback indicator.
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
    val cardCornerRadius = 24.dp

    val containerColor = when {
        isFocused -> ApexSurfaceContainerHighest
        isPlaying -> ApexSecondaryContainer.copy(alpha = 0.65f)
        else -> ApexSurfaceContainer
    }

    val restingBorder = when {
        isFocused -> BorderStroke(2.dp, ApexPrimaryMauve)
        isPlaying -> BorderStroke(1.5.dp, ApexSecondaryRoseSlate)
        else -> BorderStroke(1.dp, ApexInactiveTileBorder)
    }

    Box(
        modifier = modifier
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .apexDpadFocusRing(
                isFocused = isFocused,
                scaleFactor = 1.05f,
                cornerRadius = cardCornerRadius
            )
            .clip(RoundedCornerShape(cardCornerRadius))
            .background(containerColor)
            .border(restingBorder, RoundedCornerShape(cardCornerRadius))
            .drawBehind {
                // Active / Playing: persistent 3dp left border accent in #866E98
                if (isPlaying) {
                    val accentWidth = 5.dp.toPx()
                    drawRoundRect(
                        color = ApexTertiaryLive,
                        topLeft = Offset(0f, 0f),
                        size = Size(accentWidth, size.height),
                        cornerRadius = CornerRadius(accentWidth, accentWidth)
                    )
                }
            }
            .clickable(onClick = onClick)
            .testTag("media_card_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // 16:9 Thumbnail Box with Hotlinked Image & Duration Timestamp anchored bottom-right
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ApexSurfaceContainerLow)
                    .border(1.dp, ApexInactiveTileBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                // Hotlinked image via Coil with memory + disk caching enabled for Android 6 efficiency
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.thumbnailUrl)
                        .crossfade(false) // Avoid alpha overdraw during D-pad scroll on Android 6
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Subtle bottom scrim for high-contrast badge readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, ApexSurface.copy(alpha = 0.88f))
                            )
                        )
                )

                // Top-left Zone + Pre-Cache status pill
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(ApexSurface.copy(alpha = 0.85f))
                            .border(1.dp, ApexInactiveTileBorder, RoundedCornerShape(999.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.zoneName,
                            style = MaterialTheme.typography.labelMedium,
                            color = ApexTextPrimary
                        )
                    }

                    if (isPreCached) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(ApexTertiaryLiveTint.copy(alpha = 0.9f))
                                .border(1.dp, ApexTertiaryLive, RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "RAM/CACHE READY",
                                style = MaterialTheme.typography.labelMedium,
                                color = ApexTertiaryLight
                            )
                        }
                    }
                }

                // Active / Playing animated equalizer indicator (top-right)
                if (isPlaying) {
                    ActivePlaybackPill(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }

                // Duration Timestamp anchored in bottom-right corner (Inter font)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ApexSurface.copy(alpha = 0.92f))
                        .border(1.dp, ApexInactiveTileBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.durationFormatted,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ApexTextPrimary
                    )
                }

                // Codec & Bitrate readout anchored bottom-left
                Text(
                    text = item.bitrateMbps,
                    style = MaterialTheme.typography.labelSmall,
                    color = ApexTextMutedMetadata,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Card Title: shifts to high-contrast font weight 700 when focused
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (isFocused) ApexTextPrimary else ApexTextPrimary.copy(alpha = 0.92f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isFocused) ApexPrimaryHighLum else ApexTextMutedMetadata,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.codecInfo,
                    style = MaterialTheme.typography.labelSmall,
                    color = ApexTextMutedMetadata
                )
                Text(
                    text = if (isPlaying) "ACTIVE ON SCREEN" else if (isFocused) "[OK] PLAY" else item.scheduleSlot,
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        isPlaying -> ApexTertiaryLight
                        isFocused -> ApexPrimaryHighLum
                        else -> ApexTextMutedMetadata
                    }
                )
            }
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
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 3 animated equalizer bars
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
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelMedium,
            color = ApexTextPrimary
        )
    }
}

/**
 * Component 3: Hardware Remote Control Legend Bar
 * Docked permanently inside the bottom safe zone.
 * Displays contextual navigation help AND supports direct touch/click interaction for testing D-Pad controls:
 * - Mauve badge for primary action: [OK] Play / Select
 * - Neutral badge for navigation: [◄ ▲ ▼ ►] Move Focus
 * - Amber badge for utility: [MENU] Kiosk Options
 * - Slate badge for exit: [BACK] Return to Dashboard
 */
@Composable
fun ApexRemoteLegendBar(
    onOkClick: () -> Unit,
    onLeftClick: () -> Unit,
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    onRightClick: () -> Unit,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("remote_legend_bar"),
        color = ApexSurfaceContainerLow,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.5.dp, ApexInactiveTileBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Mauve Badge: [OK] Play / Select
            LegendInteractivePill(
                badgeText = "[OK]",
                labelText = "Play / Select",
                badgeBg = ApexPrimaryMauve,
                badgeTextColor = ApexTextPrimary,
                borderColor = ApexPrimaryHighLum,
                onClick = onOkClick,
                testTag = "legend_btn_ok"
            )

            // 2. Neutral Badge: [◄ ▲ ▼ ►] Move Focus (Interactive D-Pad cluster)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DpadMiniKey(
                    symbol = "◄",
                    contentDesc = "D-Pad Left",
                    onClick = onLeftClick,
                    testTag = "dpad_left"
                )
                DpadMiniKey(
                    symbol = "▲",
                    contentDesc = "D-Pad Up",
                    onClick = onUpClick,
                    testTag = "dpad_up"
                )
                DpadMiniKey(
                    symbol = "▼",
                    contentDesc = "D-Pad Down",
                    onClick = onDownClick,
                    testTag = "dpad_down"
                )
                DpadMiniKey(
                    symbol = "►",
                    contentDesc = "D-Pad Right",
                    onClick = onRightClick,
                    testTag = "dpad_right"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Move Focus",
                    style = MaterialTheme.typography.labelMedium,
                    color = ApexTextMutedMetadata
                )
            }

            // 3. Amber Badge: [MENU] Kiosk Options
            LegendInteractivePill(
                badgeText = "[MENU]",
                labelText = "Kiosk Options",
                badgeBg = ApexWarningAmberTint,
                badgeTextColor = ApexWarningAmber,
                borderColor = ApexWarningAmber,
                onClick = onMenuClick,
                testTag = "legend_btn_menu"
            )

            // 4. Slate Badge: [BACK] Return to Dashboard
            LegendInteractivePill(
                badgeText = "[BACK]",
                labelText = "Return to Dashboard",
                badgeBg = ApexSecondaryContainer,
                badgeTextColor = ApexTextPrimary,
                borderColor = ApexSecondaryRoseSlate,
                onClick = onBackClick,
                testTag = "legend_btn_back"
            )
        }
    }
}

@Composable
private fun LegendInteractivePill(
    badgeText: String,
    labelText: String,
    badgeBg: Color,
    badgeTextColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(badgeBg)
                .border(1.dp, borderColor, RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelMedium,
                color = badgeTextColor
            )
        }
        Text(
            text = labelText,
            style = MaterialTheme.typography.labelMedium,
            color = ApexTextPrimary
        )
    }
}

@Composable
private fun DpadMiniKey(
    symbol: String,
    contentDesc: String,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(ApexSurfaceContainerHighest)
            .border(1.dp, ApexInactiveTileBorder, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.labelMedium,
            color = ApexTextPrimary
        )
    }
}

/**
 * Component 5: Dialogs and Override Overlays
 * - Modal dialogs sit centered on a semi-opaque scrim (no Gaussian blur).
 * - Outlined by a prominent 2px solid #966A84 border and 2rem (32dp) rounded corners.
 * - All actions inside modals default D-pad focus to the non-destructive action (index 0),
 *   requiring explicit directional input to trigger destructive kiosk restarts or cache purges.
 */
@Composable
fun ApexKioskOverrideModal(
    focusedIndex: Int,
    overscanGuideActive: Boolean,
    ramUsedMb: Int,
    ramCapMb: Int,
    diskCacheUsedMb: Int,
    diskCacheCapMb: Int,
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
                .fillMaxWidth(0.78f)
                .clickable(enabled = false, onClick = {}) // Prevent click-through
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp), // 2rem radius per spec
            color = ApexSurfaceContainer,
            border = BorderStroke(2.dp, ApexPrimaryMauve) // Prominent 2px solid #966A84 border
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "KIOSK HARDWARE OVERRIDE & CACHE DIAGNOSTICS",
                            style = MaterialTheme.typography.labelLarge,
                            color = ApexPrimaryHighLum
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Android 6.0 (API 23) Low-Overhead Media Pipeline",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ApexTextPrimary
                        )
                    }

                    ApexStatusBadge(status = KioskSyncStatus.LIVE_HEALTHY)
                }

                // Hardware Telemetry Readout Box (Inter numerical font)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ApexSurfaceContainerLow)
                        .border(1.dp, ApexInactiveTileBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryStatColumn(
                        label = "RAM BUFFER CEILING",
                        value = "$ramUsedMb MB / $ramCapMb MB",
                        sub = "Strict LoadControl Cap"
                    )
                    TelemetryStatColumn(
                        label = "FLASH RING CACHE",
                        value = "$diskCacheUsedMb MB / $diskCacheCapMb MB",
                        sub = "768KB GOP Pre-Warmed"
                    )
                    TelemetryStatColumn(
                        label = "SURFACE COMPOSITOR",
                        value = "Direct HWC Overlay",
                        sub = "0 Blur / 0 Drop-Shadows"
                    )
                    TelemetryStatColumn(
                        label = "TRACK TRANSITION",
                        value = "0 ms Gapless",
                        sub = "Pre-Queued MediaCodec"
                    )
                }

                Text(
                    text = "SAFETY GUARD: D-Pad focus defaults to non-destructive Resume. Use directional keys [▼ / ►] to target destructive cache purge or kiosk node reset.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ApexTextMutedMetadata
                )

                // Non-destructive actions row (Default D-pad focus = 0)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ApexRemoteActionButton(
                        text = "Resume Kiosk Playback (Safe Default)",
                        legendKey = "OK",
                        isFocused = focusedIndex == 0,
                        onClick = { onActionClick(0) },
                        modifier = Modifier.weight(1f),
                        testTag = "modal_btn_resume"
                    )
                    ApexRemoteActionButton(
                        text = if (overscanGuideActive) "Hide Overscan Safe Guide" else "Show 4rem Overscan Safe Guide",
                        legendKey = "SAFE",
                        isFocused = focusedIndex == 1,
                        onClick = { onActionClick(1) },
                        modifier = Modifier.weight(1f),
                        testTag = "modal_btn_overscan"
                    )
                }

                // Destructive actions row (Requires explicit directional input)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ApexRemoteActionButton(
                        text = "Purge & Re-Warm 150MB Flash Cache",
                        legendKey = "PURGE",
                        isFocused = focusedIndex == 2,
                        isDestructive = true,
                        onClick = { onActionClick(2) },
                        modifier = Modifier.weight(1f),
                        testTag = "modal_btn_purge_cache"
                    )
                    ApexRemoteActionButton(
                        text = "Force Restart Kiosk Node 04-A",
                        legendKey = "RESET",
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

@Composable
private fun TelemetryStatColumn(
    label: String,
    value: String,
    sub: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = ApexTextMutedMetadata
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = ApexTextPrimary
        )
        Text(
            text = sub,
            style = MaterialTheme.typography.labelSmall,
            color = ApexTertiaryLight
        )
    }
}

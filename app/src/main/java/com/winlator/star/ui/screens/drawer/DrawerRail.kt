package com.winlator.star.ui.screens.drawer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.winlator.star.FeatureFlags
import com.winlator.star.R
import com.winlator.star.ui.TabType
import com.winlator.star.ui.theme.LocalAccentDim

/**
 * Workstream B - B1.1: the 60dp left tab rail of the in-game drawer. Pure UI - no
 * business logic. Faithfully extracted from XServerDrawer.kt (the entire BoxWithConstraints
 * block that previously lived inline) so the reconstruction is behavior-identical:
 *   - vertical scroll when the screen is too short to fit every icon,
 *   - SpaceEvenly distribution when it does fit,
 *   - top group: section tabs in [DrawerTab.topTabs] order + conditional TV pill,
 *   - bottom group: task manager / pause / exit.
 */
@Composable
fun DrawerRail(
    selectedTab: TabType,
    isPaused: Boolean,
    tvConnected: Boolean,
    castSupported: Boolean,
    onTabClick: (TabType) -> Unit,
    onTaskManagerClick: () -> Unit,
    onPauseClick: () -> Unit,
    onExitClick: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val pauseIcon = if (isPaused) R.drawable.icon_play else R.drawable.icon_pause

    BoxWithConstraints(
        modifier = Modifier
            .width(60.dp)
            .fillMaxHeight()
            .background(
                Brush.verticalGradient(
                    colors = listOf(surface, surface, surface),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            ),
    ) {
        // The rail scrolls when the screen is too short to fit every icon
        // (so the bottom Exit/Pause buttons stay reachable). When it does
        // fit, heightIn(min) + SpaceEvenly reproduces the distributed look.
        val railMinHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = railMinHeight)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                // Top group: section tabs
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DrawerTab.topTabs.forEachIndexed { index, tab ->
                        val selected = selectedTab == tab.tabType
                        if (index > 0) Spacer(Modifier.height(6.dp))
                        // HUD renders as the "FPS" pill, the other five as icon pills.
                        if (tab == DrawerTab.Hud) {
                            FpsTabButton(selected) {
                                onTabClick(tab.tabType)
                            }
                        } else {
                            val iconRes = tab.iconRes
                                ?: error("Unexpected text-pill tab ${tab.tabType} in top group")
                            TabIconButton(iconRes, selected) {
                                onTabClick(tab.tabType)
                            }
                        }
                    }
                    // TV tab renders only while the feature flag is on AND a TV/caster
                    // is actually connected (belt-and-braces on top of the
                    // controller/caster never being constructed).
                    if (FeatureFlags.TV_OUTPUT_ENABLED && (tvConnected || castSupported)) {
                        Spacer(Modifier.height(6.dp))
                        TvTabButton(selectedTab == TabType.TV) {
                            onTabClick(TabType.TV)
                        }
                    }
                }

                // Bottom group: task manager / pause / exit
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(2.dp)
                            .background(accent, RoundedCornerShape(1.dp))
                    )

                    Spacer(Modifier.height(10.dp))

                    TabIconButton(R.drawable.icon_task_manager, selectedTab == TabType.TASK_MANAGER) {
                        onTaskManagerClick()
                    }
                    Spacer(Modifier.height(6.dp))
                    TabIconButton(pauseIcon, isSelected = false) {
                        onPauseClick()
                    }
                    Spacer(Modifier.height(6.dp))
                    TabIconButton(R.drawable.icon_exit, isSelected = false) {
                        onExitClick()
                    }
                }
            }
        }
    }
}

// ───── Modern Tab Button (extracted verbatim from XServerDrawer.kt) ─────

@Composable
private fun TabIconButton(iconRes: Int, isSelected: Boolean, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val accentDim = LocalAccentDim.current
    // Selected = filled accent pill (accent → dim), matching the rebuild preview.
    val bgBrush = if (isSelected)
        Brush.verticalGradient(listOf(accent, accentDim))
    else
        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))

    val borderColor = if (isSelected) accent.copy(alpha = 0.6f) else Color(0xFF333333)
    val tintColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgBrush, RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Canvas(Modifier.size(44.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.25f), Color.Transparent),
                        radius = size.minDimension / 2f
                    ),
                    radius = size.minDimension / 2f
                )
            }
        }
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun FpsTabButton(isSelected: Boolean, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val accentDim = LocalAccentDim.current
    val bgBrush = if (isSelected)
        Brush.verticalGradient(listOf(accent, accentDim))
    else
        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))

    val borderColor = if (isSelected) accent.copy(alpha = 0.6f) else Color(0xFF333333)
    val textColor = if (isSelected) Color.White else accent

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgBrush, RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Canvas(Modifier.size(44.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.25f), Color.Transparent),
                        radius = size.minDimension / 2f
                    ),
                    radius = size.minDimension / 2f
                )
            }
        }
        Text(
            text = "FPS",
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

// TV tab: a text "TV" pill (mirrors the FPS tab) instead of an icon.
@Composable
private fun TvTabButton(isSelected: Boolean, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val accentDim = LocalAccentDim.current
    val bgBrush = if (isSelected)
        Brush.verticalGradient(listOf(accent, accentDim))
    else
        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))

    val borderColor = if (isSelected) accent.copy(alpha = 0.6f) else Color(0xFF333333)
    val textColor = if (isSelected) Color.White else accent

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgBrush, RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Canvas(Modifier.size(44.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.25f), Color.Transparent),
                        radius = size.minDimension / 2f
                    ),
                    radius = size.minDimension / 2f
                )
            }
        }
        Text(
            text = "TV",
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}
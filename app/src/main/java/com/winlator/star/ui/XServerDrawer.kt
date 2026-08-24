package com.winlator.star.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import com.winlator.star.R
import com.winlator.star.container.Container
import com.winlator.star.reshade.ReshadeLoadout
import com.winlator.star.reshade.ReshadeManager
import com.winlator.star.ui.components.ColorPicker
import com.winlator.star.ui.screens.MenuItemDivider
import com.winlator.star.ui.screens.drawer.AdvancedTab
import com.winlator.star.ui.screens.drawer.AudioTab
import com.winlator.star.ui.screens.drawer.GraphicsTab
import com.winlator.star.ui.screens.drawer.ReshadeTab
import com.winlator.star.ui.screens.drawer.ControlsTab
import com.winlator.star.ui.screens.drawer.DrawerRail
import com.winlator.star.ui.screens.drawer.DrawerSectionHeader
import com.winlator.star.ui.screens.drawer.DrawerToggleRow
import com.winlator.star.ui.screens.drawer.DrawerLabeledSlider
import com.winlator.star.ui.screens.drawer.DrawerAccentButton
import com.winlator.star.ui.screens.drawer.DrawerModeChipGrid
import com.winlator.star.ui.screens.drawer.DrawerToggleChipGrid
import com.winlator.star.ui.screens.drawer.DrawerToggleChipItem
import com.winlator.star.ui.screens.drawer.DrawerPrettyDxWrapper
import com.winlator.star.ui.screens.drawer.DrawerPrettyRenderer
import com.winlator.star.ui.screens.drawer.DrawerTidyDevice
import com.winlator.star.ui.screens.drawer.DrawerRefreshRateSlider
import com.winlator.star.ui.screens.drawer.DrawerIntSlider
import com.winlator.star.ui.screens.drawer.DrawerSeShaderToggle
import com.winlator.star.ui.screens.drawer.DrawerFullscreenModeButtons
import com.winlator.star.ui.screens.drawer.DrawerUpscalerModeButtons
import com.winlator.star.ui.screens.drawer.DrawerReshadeDropdown
import com.winlator.star.ui.screens.drawer.TaskManagerTab
import com.winlator.star.ui.screens.outlinedMenuCard
import com.winlator.star.ui.theme.LocalAccentDim
import com.winlator.star.ui.theme.WinlatorTheme
import com.winlator.star.widget.perfhud.parseHudOutline
import com.winlator.star.widget.exportHudDiagnostics

// Accent colors route to the live MaterialTheme.colorScheme (primary/surface) so the drawer
// follows the user's theme preset / custom accent. The dim accent (low-emphasis fills/borders/
// tracks) routes to LocalAccentDim.current — AMOLED maps that to the exact legacy #002277 so the
// default look stays identical, while other presets/custom accents recolor it.
// Phase 2: the neutral surface/text/divider constants are gone — call sites read
// MaterialTheme.colorScheme directly (surface / onSurface / onSurfaceVariant / outline) so the
// whole drawer (every tab) follows the theme. AMOLED's tokens match the legacy neutrals closely,
// so the default look stays near-identical.
// PureBlack is kept only for spots that need a true literal black that must NOT theme (the
// color-picker knob outline); panel/rail backgrounds route to colorScheme.surface instead.
private val PureBlack = Color(0xFF000000)
private val ToggleTrackOff = Color(0xFF333333)
private val ToggleThumbOff = Color(0xFF666666)

fun setupComposeView(view: ComposeView) {
    view.setContent {
        WinlatorTheme {
            XServerDrawer()
        }
    }
}

@Composable
fun XServerDrawer() {
    val state = XServerDrawerState
    val selectedTab by state.selectedTab.collectAsState()
    val isPaused by state.isPaused.collectAsState()
    val tvConnected by state.tvConnected.collectAsState()
    val castSupported by state.castSupported.collectAsState()
    val accent = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxHeight()
            .width(380.dp)
            .background(surface)
    ) {
        DrawerRail(
            selectedTab = selectedTab,
            isPaused = isPaused,
            tvConnected = tvConnected,
            castSupported = castSupported,
            onTabClick = { handleTabClick(it, state) },
            onTaskManagerClick = {
                state.selectTab(TabType.TASK_MANAGER)
                state.onTaskManager?.run()
            },
            onPauseClick = { state.onPauseResume?.run(); state.onClose?.run() },
            onExitClick = { state.onExit?.run() },
        )

        // Accent seam between the tab rail and its content — mirrors the HUD's "Accent" outline
        // (full-height cyan), matching the prototype's rail/drawer divider.
        Box(
            modifier = Modifier
                .width(1.5.dp)
                .fillMaxHeight()
                .background(accent)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
        ) {
            when (selectedTab) {
                TabType.GRAPHICS -> GraphicsTab(state)
                TabType.HUD -> HudContent(state)
                TabType.RESHADE -> ReshadeTab(state)
                TabType.CONTROLS -> ControlsTab(state)
                TabType.ADVANCED -> AdvancedTab(state)
                TabType.TASK_MANAGER -> TaskManagerTab()
                TabType.TV -> TvContent(state)
                TabType.AUDIO -> AudioTab(state)
            }
        }
    }
}

private fun handleTabClick(tab: TabType, state: XServerDrawerState) {
    state.selectTab(tab)
}

// ───── TV / External Display tab ─────
// Version A: game on the TV, handheld as the controller. This minimal panel exposes the display
// controls; picture/latency controls (aspect, overscan, latency mode, audio) land in a later pass.
@Composable
private fun TvContent(state: XServerDrawerState) {
    val tvConnected by state.tvConnected.collectAsState()
    val displayName by state.tvDisplayName.collectAsState()
    val playOnTv by state.tvPlayOnTv.collectAsState()
    val autoSwap by state.tvAutoSwap.collectAsState()
    val onExternal by state.tvGameOnExternal.collectAsState()
    val modes by state.tvModes.collectAsState()
    val currentModeId by state.tvCurrentModeId.collectAsState()
    val hdr by state.tvHdr.collectAsState()

    // ───────────── Wireless cast (screen mirroring — no app on the TV) ─────────────
    SectionHeader("Cast to a TV (wireless)")
    Text(
        text = "⚠ EXPERIMENTAL · VIDEO ONLY — wireless streaming is new: the picture runs a few seconds " +
            "behind, there's no TV sound yet (game audio stays on this device), and it may take a moment " +
            "to start or need a second try.",
        color = MaterialTheme.colorScheme.error,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
    Text(
        text = "Stream the game's picture to a Google TV / Chromecast on your Wi-Fi — pick one in the app, " +
            "nothing to install on the TV. Tap the “?” inside for how it works and the trade-offs.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    AccentButton("Cast to a TV", Modifier.fillMaxWidth()) {
        state.onOpenCastPicker?.run()
    }
    Text(
        text = "For the lowest lag, a wired USB-C→HDMI cable is still best.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 4.dp)
    )

    // ───────────── Wired / external display (only when one is actually connected) ─────────────
    if (tvConnected) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 12.dp))
        SectionHeader("TV / External Display")

        Text(
            text = if (displayName.isNotBlank()) "Connected: $displayName" else "External display connected",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Output resolution + refresh rate of the TV (e.g. switch 4K@30 → 1080p@60 for smoother play).
        if (modes.isNotEmpty()) {
            val labels = modes.map { it.label }
            val selectedIdx = modes.indexOfFirst { it.id == currentModeId }.let { if (it >= 0) it else 0 }
            ReshadeDropdown("Display mode (resolution & refresh)", labels, selectedIdx) { i ->
                val id = modes[i].id
                state.setTvCurrentModeId(id)
                state.onTvModeChange?.accept(id)
            }
            Spacer(Modifier.height(6.dp))
        }

        if (hdr.isNotBlank()) {
            Text(
                text = "HDR supported by display: $hdr",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        ToggleRow("Play on TV", playOnTv) {
            state.setTvPlayOnTv(it)
            state.onTvPlayOnTvChange?.accept(it)
        }
        ToggleRow("Auto-switch on connect", autoSwap, enabled = playOnTv) {
            state.setTvAutoSwap(it)
            state.onTvAutoSwapChange?.accept(it)
        }

        Spacer(Modifier.height(12.dp))

        if (onExternal) {
            AccentButton("Bring game back to handheld", Modifier.fillMaxWidth()) {
                state.onBringBackFromTv?.run()
            }
        } else {
            AccentButton("Move game to TV", Modifier.fillMaxWidth()) {
                state.onMoveToTv?.run()
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // Rebuild the audio route — sound can drop after backgrounding or an HDMI route change.
    AccentButton("Reset audio", Modifier.fillMaxWidth()) {
        state.onResetAudio?.run()
    }
    Text(
        text = "Fixes lost sound after switching apps.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 4.dp)
    )


    val rendererIsVulkan by state.rendererIsVulkan.collectAsState()

    // ───────────── Picture ─────────────
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 12.dp))
    SectionHeader("Picture")

    // Aspect on TV — reuses the same fullscreen-mode cycle as the handheld (OFF / FIT / STRETCH).
    val fullscreenMode by state.fullscreenMode.collectAsState()
    Text("Aspect", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
    FullscreenModeButtons(fullscreenMode) { mode ->
        state.setFullscreenMode(mode)
        state.onSetFullscreenMode?.accept(mode)
    }

    // Overscan / safe area (v2) — pads the game inward on TVs that crop the edges.
    Spacer(Modifier.height(8.dp))
    val overscan by state.tvOverscan.collectAsState()
    var overscanVal by remember(overscan) { mutableIntStateOf(overscan) }
    IntSlider("Overscan / safe area", overscanVal, 0..8,
        onValueChange = { overscanVal = it },
        onValueChangeFinished = {
            state.setTvOverscan(overscanVal)
            state.onTvOverscanChange?.accept(overscanVal)
        },
        steps = 7, enabled = true)
    Text("Shrinks the picture inward if your TV cuts off the edges.",
        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp))

    // Scaling filter (v1) — GL EffectComposer only; grayed on the Vulkan renderer.
    if (rendererIsVulkan) {
        Text("Scaling filter is available on the OpenGL renderer.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp))
    } else {
        Text("Scaling filter", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp, bottom = 6.dp))
        val initGlUpscalerMode by XServerDialogState.glUpscalerMode.collectAsState()
        var glUpscalerMode by remember(initGlUpscalerMode) { mutableIntStateOf(initGlUpscalerMode) }
        UpscalerModeButtons(glUpscalerMode, true) {
            glUpscalerMode = it
            XServerDialogState.onGlUpscalerApply?.invoke(it)
        }
    }

    // ───────────── Latency & pacing ─────────────
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 12.dp))

    // Latency mode (present mode). PresentModeSection self-gates to the Vulkan renderer.
    if (rendererIsVulkan) {
        PresentModeSection(state)
    } else {
        SectionHeader("Latency")
        Text("Latency mode (V-Sync / Low latency) is available on the Vulkan renderer.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 6.dp))
    }

    // Frame cap — drives the standalone host FPS limiter (shared with the HUD tab).
    Spacer(Modifier.height(6.dp))
    val fpsLimiterEnabled by state.fpsLimiterEnabled.collectAsState()
    val fpsLimit by state.fpsLimit.collectAsState()
    val capOptions = listOf("Off", "30 FPS", "60 FPS", "90 FPS", "120 FPS")
    val capValues = listOf(0, 30, 60, 90, 120)
    val capIdx = if (!fpsLimiterEnabled) 0 else capValues.indexOf(fpsLimit).let { if (it >= 0) it else 0 }
    ReshadeDropdown("Frame cap", capOptions, capIdx) { i ->
        val v = capValues[i]
        state.setFpsLimiterEnabled(v > 0)
        if (v > 0) state.setFpsLimit(v)
        state.onFpsLimitChange?.run()
    }

    // Frame generation — the full reused section (engine picker + multiplier + models).
    Spacer(Modifier.height(6.dp))
    FrameGenSection(state)

    // TV Game Mode tip (biggest wired-latency win is TV-side; we can only advise).
    Text("Tip: enable Game Mode on your TV for the lowest input lag.",
        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp,
        modifier = Modifier.padding(top = 8.dp))

    // ───────────── Audio & power ─────────────
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 12.dp))
    SectionHeader("Audio & power")

    val audioOut by state.tvAudioOut.collectAsState()
    ReshadeDropdown("Audio output", listOf("Follow system", "TV / HDMI", "Handheld"), audioOut) { i ->
        state.setTvAudioOut(i)
        state.onTvAudioOutChange?.accept(i)
    }
    Text("Experimental — the guest audio route may not always follow.",
        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp))

    val dimHandheld by state.tvDimHandheld.collectAsState()
    ToggleRow("Dim handheld while on TV", dimHandheld) {
        state.setTvDimHandheld(it)
        state.onTvDimHandheldChange?.accept(it)
    }
    Text("Saves battery and heat by dimming the phone screen.",
        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp,
        modifier = Modifier.padding(top = 2.dp))

    // ───────────── Advanced ─────────────
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 12.dp))
    SectionHeader("Advanced")

    val renderRes by state.tvRenderRes.collectAsState()
    ReshadeDropdown("TV render resolution", listOf("Match TV", "Match handheld", "1080p", "1440p"), renderRes) { i ->
        state.setTvRenderRes(i)
        state.onTvRenderResChange?.accept(i)
    }
    Text("Applies on the next game launch (the render resolution is fixed at startup).",
        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp,
        modifier = Modifier.padding(top = 2.dp))

    // ───────────── Streaming (v3 — WiFi caster, not yet available) ─────────────
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 12.dp))
    val disabledColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Text("Streaming", style = MaterialTheme.typography.titleSmall.copy(
        fontSize = 15.sp, fontWeight = FontWeight.Bold), color = disabledColor)
    Text("Wireless streaming to a TV without a cable (bitrate, codec, transport) is coming in a " +
        "future update. Wired HDMI / DeX casting works today via the controls above.",
        color = disabledColor, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
    listOf("Bitrate", "Codec (H.264 / HEVC)", "Transport (WebRTC / DLNA)", "Stream resolution & FPS").forEach {
        Text("• $it — requires WiFi streaming", color = disabledColor, fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp))
    }

    Spacer(Modifier.height(12.dp))
}

// ───── Shared drawer primitives ─────
// Extracted to ui/screens/drawer/DrawerComponents.kt. These thin, same-signature
// @Composable wrappers keep every existing call site (Graphics/HUD/ReShade/Controls/
// Advanced/Task Manager/TV tabs) untouched while the real bodies live elsewhere.

@Composable
private fun SectionHeader(title: String) {
    DrawerSectionHeader(title)
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit) {
    DrawerToggleRow(label, checked, enabled, onCheckedChange)
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    steps: Int = 0,
    enabled: Boolean = true,
    format: (Float) -> String = { "%.0f".format(it) }
) {
    DrawerLabeledSlider(label, value, valueRange, onValueChange, onValueChangeFinished, steps, enabled, format)
}

@Composable
private fun AccentButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    DrawerAccentButton(text, modifier, onClick)
}

@Composable
private fun FullscreenModeButtons(selected: Int, onSelect: (Int) -> Unit) {
    DrawerFullscreenModeButtons(selected, onSelect)
}

@Composable
private fun UpscalerModeButtons(selected: Int, enabled: Boolean, onSelect: (Int) -> Unit) {
    DrawerUpscalerModeButtons(selected, enabled, onSelect)
}

@Composable
private fun FrameGenSection(state: XServerDrawerState) {
    DrawerFrameGenSection(state)
}

@Composable
private fun ReshadeDropdown(label: String, options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    DrawerReshadeDropdown(label, options, selected, onSelect)
}

// Multi-select cousin of FullscreenModeButtons: each item toggles independently, but shares the exact
// box style (accent fill + bold black text ON; black bg + accentDim 1dp border + accent medium text OFF)
// and the aligned equal-width grid (weight(1f), short rows padded with Spacer so widths stay equal).
// Callers build the list of currently-VISIBLE chips FIRST, then this chunks per row — so per-style
// gating never leaves holes or misaligns the grid.
// enabled=false greys the WHOLE grid and swallows taps, the same way DrawerVibrationModeButtons does it —
// for rows that stay on screen because they still explain something, but can't be acted on yet.
// disabledIndices greys INDIVIDUAL chips (indices into the flat items list) for the case where one
// option is unreachable in the current configuration but the rest of the row is still live — greyed
// rather than dropped, so the row doesn't reflow and the option is visibly still a thing that exists.
@Composable
private fun ModeChipGrid(items: List<Triple<String, Boolean, () -> Unit>>, perRow: Int,
                         enabled: Boolean = true, disabledIndices: Set<Int> = emptySet()) {
    DrawerModeChipGrid(items, perRow, enabled, disabledIndices)
}

// One on/off chip in a ToggleChipGrid — same tuple ToggleRow takes (label + checked + enabled +
// callback), just laid out as a chip instead of a full-width switch row.
private fun ToggleChipItem(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onToggle: (Boolean) -> Unit
): DrawerToggleChipItem = DrawerToggleChipItem(label, checked, enabled, onToggle)

// Compact stand-in for a run of ToggleRows: same chip language as ModeChipGrid above (accent fill +
// bold black text ON; black bg + accentDim 1dp border + accent medium text OFF, equal widths, short
// rows padded with Spacer), but every chip toggles independently. Packing adjacent toggles 2-4 per
// row is where the vertical space comes back — a Switch row costs ~4x the height of a chip.
// Disabled chips keep ToggleRow's alpha-0.4 grey-out and swallow taps.
@Composable
private fun ToggleChipGrid(items: List<DrawerToggleChipItem>, perRow: Int = 3) {
    DrawerToggleChipGrid(items, perRow)
}

// Manual refresh-rate slider — snaps to [Off] + each supported panel rate (which may be unevenly
// spaced, e.g. 60/90/120/144). Off (0) = no manual lock. The label tracks the snapped value live
// while dragging; the actual panel rate is applied on release so we don't flash through modes mid-drag.
// Greyed when disabled (Auto on or display not VRR-capable).
@Composable
private fun RefreshRateSlider(rates: List<Int>, selected: Int, enabled: Boolean, autoRate: Int, onSelect: (Int) -> Unit) {
    DrawerRefreshRateSlider(rates, selected, enabled, autoRate, onSelect)
}

@Composable
private fun IntSlider(label: String, value: Int, valueRange: IntRange, onValueChange: (Int) -> Unit, onValueChangeFinished: (() -> Unit)? = null, steps: Int = -1, enabled: Boolean = true) {
    DrawerIntSlider(label, value, valueRange, onValueChange, onValueChangeFinished, steps, enabled)
}

@Composable
private fun SeShaderToggle(label: String, checked: Boolean, enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit) {
    DrawerSeShaderToggle(label, checked, enabled, onCheckedChange)
}

@OptIn(ExperimentalMaterial3Api::class)
// ───── HUD Tab ─────

@Composable
private fun HudContent(state: XServerDrawerState) {
    val accent = MaterialTheme.colorScheme.primary
    val fpsConfig by state.fpsConfig.collectAsState()

    // Re-read the live display refresh rate when this tab opens so the "Rate" readout is fresh on
    // open; the display listener keeps it current while the drawer stays open.
    LaunchedEffect(Unit) { state.onRefreshRatePoll?.run() }

    SectionHeader("HUD")

    // ── FPS Limiter state (standalone host-side cap; output-cap = on-screen fps, independent of
    //    frame gen). Declared here; its UI lives in the Performance accordion section below. ──
    val fpsLimiterEnabled by state.fpsLimiterEnabled.collectAsState()
    val initFpsLimit by state.fpsLimit.collectAsState()
    var limiterOn by remember(fpsLimiterEnabled) { mutableStateOf(fpsLimiterEnabled) }
    var limitVal by remember(initFpsLimit) { mutableIntStateOf(initFpsLimit) }
    fun applyLimiter() {
        state.setFpsLimiterEnabled(limiterOn)
        state.setFpsLimit(limitVal)
        // Standalone limiter: applies live to the host renderer regardless of frame-gen engine.
        state.onFpsLimitChange?.run()
    }

    // ── Refresh rate state: Auto (match FPS / VRR) + manual snap to a supported panel rate.
    //    Declared here; its UI lives in the Performance accordion section below. ──
    val matchRefreshRate by state.matchRefreshRate.collectAsState()
    val vrrSupported by state.vrrSupported.collectAsState()
    val manualRefreshRate by state.manualRefreshRate.collectAsState()
    val supportedRefreshRates by state.supportedRefreshRates.collectAsState()
    val currentRefreshRate by state.currentRefreshRate.collectAsState()
    var matchRefreshOn by remember(matchRefreshRate) { mutableStateOf(matchRefreshRate) }

    fun parseConfig(s: String): Map<String, String> {
        if (s.isEmpty()) return emptyMap()
        val map = mutableMapOf<String, String>()
        s.split(",").forEach { part ->
            val eq = part.indexOf('=')
            if (eq >= 0) map[part.substring(0, eq)] = part.substring(eq + 1)
        }
        return map
    }

    val cfg = remember(fpsConfig) { parseConfig(fpsConfig) }
    // Read with fallback across classic + gamehub key names (mirrors the container dialog).
    fun b(k: String, fb: String, d: String) = (cfg[k] ?: cfg[fb] ?: d) == "1"

    // Every HUD control is keyed on `cfg` so the drawer always mirrors the
    // setup currently in use: when a container launches, the overlay honors the
    // saved config and these re-initialize from that same live config (just like
    // the FPS-limiter rows above). Un-keyed remembers would capture stale values
    // once and drift from what's actually on screen.
    // Orientation is flipped by tapping the HUD in-game; preserve it on write-back.
    val hudMode = remember(cfg) { cfg.getOrDefault("hudMode", "vertical") }

    // Master HUD on/off. When off, the activity keeps every overlay style GONE even while a game
    // window is bound (see XServerDisplayActivity.hudCounterEnabled). The HUD group below hides.
    var hudEnabled by remember(cfg) { mutableStateOf(b("hudEnabled", "hudEnabled", "1")) }

    // 4-way HUD style: classic | gamehub | gamenative | fusion.
    val styles = listOf("classic", "gamehub", "gamenative", "fusion")
    var hudStyle by remember(cfg) { mutableStateOf(cfg.getOrDefault("hudStyle", "fusion")) }
    val gameHub = hudStyle == "gamehub"
    val gameNative = hudStyle == "gamenative"
    val fusion = hudStyle == "fusion"
    val rich = gameHub || gameNative || fusion   // opacity + FPS graph + GPU model + color/outline
    // Fusion size mode (also live-cycled by tapping the Fusion HUD in-game).
    val fusionSizes = listOf("full", "tiles", "pill", "minimal", "mega")
    var fusionSize by remember(cfg) { mutableStateOf(cfg.getOrDefault("hudSize", "pill")) }
    // Chips the selected Fusion size actually renders (single source of truth in FusionSize).
    val fusionChips = com.winlator.star.widget.fusionhud.FusionSize.from(fusionSize).supportedChips()
    val gpuModelDefault = if (cfg.getOrDefault("hudStyle", "fusion") == "fusion") "1" else "0"
    val clockDefault = if (cfg.getOrDefault("hudStyle", "fusion") == "fusion") "1" else "0"
    var showFPS by remember(cfg) { mutableStateOf(b("showFPS", "showFPS", "1")) }
    var showGraph by remember(cfg) { mutableStateOf(b("showFPSGraph", "showFPSGraph", "0")) }
    var showCPU by remember(cfg) { mutableStateOf(b("showCPUUsage", "showCPULoad", "1")) }
    var showGPU by remember(cfg) { mutableStateOf(b("showGPULoad", "showGPULoad", "1")) }
    var showRAM by remember(cfg) { mutableStateOf(b("showRAM", "showRAM", "1")) }
    var showPower by remember(cfg) { mutableStateOf(b("showPower", "showPower", "1")) }
    var showTemp by remember(cfg) { mutableStateOf(b("showTemp", "showBatteryTemp", "1")) }
    var showEngine by remember(cfg) { mutableStateOf(b("showEngine", "showRenderer", "1")) }
    var showGpuModel by remember(cfg) { mutableStateOf(b("showGpuModel", "showGpuModel", gpuModelDefault)) }
    var dualBattery by remember(cfg) { mutableStateOf(b("hudDualBattery", "hudDualBattery", "0")) }
    // GameNative-only extra metrics (absent = off is the intended default).
    var showGpuTemp by remember(cfg) { mutableStateOf(b("showGpuTemp", "showGpuTemp", "0")) }
    var showBattery by remember(cfg) { mutableStateOf(b("showBattery", "showBattery", "0")) }
    var showRuntime by remember(cfg) { mutableStateOf(b("showRuntime", "showRuntime", "0")) }
    var showClock by remember(cfg) { mutableStateOf(b("showClock", "showClock", clockDefault)) }
    var showCpuGraph by remember(cfg) { mutableStateOf(b("showCPUGraph", "showCPUGraph", "0")) }
    var showGpuGraph by remember(cfg) { mutableStateOf(b("showGPUGraph", "showGPUGraph", "0")) }
    // Fusion extra metrics + global lock (defaults match Container.DEFAULT_FPS_COUNTER_CONFIG).
    var showVram by remember(cfg) { mutableStateOf(b("showVram", "showVram", "1")) }
    var showLow001 by remember(cfg) { mutableStateOf(b("showLow001", "showLow001", "1")) }
    var fpsDecimal by remember(cfg) { mutableStateOf(b("fpsDecimal", "fpsDecimal", "1")) }
    var hudLocked by remember(cfg) { mutableStateOf(b("hudLocked", "hudLocked", "0")) }
    // Fusion Mega-only metrics.
    var showPerCore by remember(cfg) { mutableStateOf(b("showPerCore", "showPerCore", "1")) }
    var showSwap by remember(cfg) { mutableStateOf(b("showSwap", "showSwap", "1")) }
    var showNet by remember(cfg) { mutableStateOf(b("showNet", "showNet", "1")) }
    var showResolution by remember(cfg) { mutableStateOf(b("showResolution", "showResolution", "1")) }
    var showProton by remember(cfg) { mutableStateOf(b("showProton", "showProton", "1")) }
    var showWrapper by remember(cfg) { mutableStateOf(b("showWrapper", "showWrapper", "1")) }
    var showDxVer by remember(cfg) { mutableStateOf(b("showDxVer", "showDxVer", "1")) }
    var showSession by remember(cfg) { mutableStateOf(b("showSession", "showSession", "1")) }
    // Temperature display: unit, plus danger bands as a single 3-way (Off / Auto / Manual) rather
    // than two toggles — "banding on but auto off" and "banding off but auto on" aren't distinct
    // states worth exposing. Auto reads the device's own thermal trip points.
    var tempUnitF by remember(cfg) { mutableStateOf(cfg.getOrDefault("tempUnit", "c").equals("f", true)) }
    var tempBands by remember(cfg) { mutableStateOf(cfg.getOrDefault("tempBands", "1") != "0") }
    var tempAuto by remember(cfg) { mutableStateOf(cfg.getOrDefault("tempAuto", "1") != "0") }
    var tempRedCpu by remember(cfg) { mutableFloatStateOf(cfg.getOrDefault("tempRedCpu", "90").toFloatOrNull() ?: 90f) }
    var tempRedGpu by remember(cfg) { mutableFloatStateOf(cfg.getOrDefault("tempRedGpu", "90").toFloatOrNull() ?: 90f) }
    var tempRedBat by remember(cfg) { mutableFloatStateOf(cfg.getOrDefault("tempRedBat", "48").toFloatOrNull() ?: 48f) }

    var scaleValue by remember(cfg) { mutableFloatStateOf(cfg.getOrDefault("hudScale", Container.DEFAULT_HUD_SCALE.toString()).toFloatOrNull() ?: Container.DEFAULT_HUD_SCALE.toFloat()) }
    var opacityValue by remember(cfg) { mutableFloatStateOf(cfg.getOrDefault("hudOpacity", "80").toFloatOrNull() ?: 80f) }
    var transValue by remember(cfg) { mutableFloatStateOf(cfg.getOrDefault("hudTransparency", "0").toFloatOrNull() ?: 0f) }

    val skins = listOf("classic", "neon", "mono")
    val colors = listOf("soft", "mid", "vivid")
    var skin by remember(cfg) { mutableStateOf(cfg.getOrDefault("hudSkin", "classic")) }
    var color by remember(cfg) { mutableStateOf(cfg.getOrDefault("hudColor", "mid")) }
    // hudOutline is a 0..100 intensity (legacy off/soft/strong strings map via parseHudOutline).
    var outlineValue by remember(cfg) { mutableFloatStateOf(parseHudOutline(cfg.getOrDefault("hudOutline", "40")).toFloat()) }
    var outlineAccent by remember(cfg) { mutableStateOf(cfg.getOrDefault("hudOutlineAccent", "1") == "1") }

    fun i(v: Boolean) = if (v) "1" else "0"
    // Identical key set to ContainerDetailScreen.FpsCounterConfigDialog.buildConfig(),
    // so the in-game drawer and the pre-launch dialog stay fully interchangeable.
    fun buildConfig(): String = listOf(
        "hudStyle=$hudStyle",
        "hudEnabled=${i(hudEnabled)}",
        "hudSize=$fusionSize",
        "hudLocked=${i(hudLocked)}",
        "showVram=${i(showVram)}",
        "showLow001=${i(showLow001)}",
        "fpsDecimal=${i(fpsDecimal)}",
        "showPerCore=${i(showPerCore)}",
        "showSwap=${i(showSwap)}",
        "showNet=${i(showNet)}",
        "showResolution=${i(showResolution)}",
        "showProton=${i(showProton)}",
        "showWrapper=${i(showWrapper)}",
        "showDxVer=${i(showDxVer)}",
        "showSession=${i(showSession)}",
        "hudMode=$hudMode",
        "showFPS=${i(showFPS)}",
        "showFPSGraph=${i(showGraph)}",
        "showCPUUsage=${i(showCPU)}",
        "showCPULoad=${i(showCPU)}",
        "showGPULoad=${i(showGPU)}",
        "showRAM=${i(showRAM)}",
        "showPower=${i(showPower)}",
        "showTemp=${i(showTemp)}",
        "showBatteryTemp=${i(showTemp)}",
        "showEngine=${i(showEngine)}",
        "showRenderer=${i(showEngine)}",
        "showGpuModel=${i(showGpuModel)}",
        "hudDualBattery=${i(dualBattery)}",
        "showGpuTemp=${i(showGpuTemp)}",
        "showBattery=${i(showBattery)}",
        "showRuntime=${i(showRuntime)}",
        "showClock=${i(showClock)}",
        "showCPUGraph=${i(showCpuGraph)}",
        "showGPUGraph=${i(showGpuGraph)}",
        "tempUnit=${if (tempUnitF) "f" else "c"}",
        "tempBands=${i(tempBands)}",
        "tempAuto=${i(tempAuto)}",
        "tempRedCpu=${tempRedCpu.toInt()}",
        "tempRedGpu=${tempRedGpu.toInt()}",
        "tempRedBat=${tempRedBat.toInt()}",
        "hudSkin=$skin",
        "hudColor=$color",
        "hudOutline=${outlineValue.toInt()}",
        "hudOutlineAccent=${if (outlineAccent) 1 else 0}",
        "hudScale=${scaleValue.toInt()}",
        "hudOpacity=${opacityValue.toInt()}",
        "hudTransparency=${transValue.toInt()}",
    ).joinToString(",")

    fun apply() { state.onFpsConfigApply?.invoke(buildConfig()) }

    // ═══ Master toggle: hides every HUD group below when off (matches the approved prototype). ═══
    ToggleRow("Show HUD", hudEnabled) { hudEnabled = it; apply() }

    // ── Performance group: always shown. The limiter + refresh live here regardless of the HUD. ──
    HudGroupLabel("Performance")
    CollapsibleSection("Frame rate & refresh", lead = "always on", initiallyExpanded = true) {
        Text("FPS Limiter", color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        ToggleRow("Limit FPS", limiterOn) { limiterOn = it; applyLimiter() }
        if (limiterOn) {
            LabeledSlider(
                "Max FPS", limitVal.toFloat(), 10f..200f,
                { limitVal = it.roundToInt() }, { applyLimiter() },
                format = { "${it.roundToInt()}" }
            )
            // Quick presets: set the cap in one tap. Shares limitVal with the slider above, so the
            // slider thumb snaps to the picked value (and the matching chip highlights on any value).
            Spacer(Modifier.height(6.dp))
            ModeChipGrid(
                listOf(30, 60, 90, 120).map { preset ->
                    Triple("$preset", limitVal == preset) { limitVal = preset; applyLimiter() }
                },
                perRow = 4
            )
            Text(
                "Caps on-screen FPS. Works with any frame-gen engine or none.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        Spacer(Modifier.height(14.dp))
        Text("Refresh rate", color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        // Auto (match FPS) == the existing VRR toggle; behavior unchanged.
        ToggleRow("Auto (match FPS)", matchRefreshOn && vrrSupported, enabled = vrrSupported) {
            matchRefreshOn = it
            state.setMatchRefreshRate(it)
            state.onMatchRefreshChange?.run()
        }
        // Manual rate slider: selectable only when Auto is OFF and the panel can switch rates.
        if (supportedRefreshRates.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            RefreshRateSlider(supportedRefreshRates, manualRefreshRate, vrrSupported && !matchRefreshOn, currentRefreshRate) { rate ->
                state.setManualRefreshRate(rate)
                state.onManualRefreshChange?.run()
            }
        }
        Text(
            when {
                !vrrSupported ->
                    "Unavailable — this display has a single refresh rate, so there's nothing to match."
                matchRefreshOn ->
                    "Auto is on — the display follows your FPS."
                manualRefreshRate > 0 ->
                    "Display locked to ${manualRefreshRate} Hz."
                else ->
                    "Pick a rate to lock the display, or turn Auto on to follow your FPS."
            },
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )
    }

    // ── HUD group: hidden entirely when the master toggle is off. ──
    if (hudEnabled) {
        HudGroupLabel("HUD")

        CollapsibleSection("Style & Size") {
            HudChipRow("HUD style", listOf("Classic", "GameHub", "GameNative", "Fusion"), styles.indexOf(hudStyle).coerceAtLeast(0)) { hudStyle = styles[it]; apply() }
            Text(
                when (hudStyle) {
                    "gamehub" -> "Rich overlay: skins, colored fields, live FPS graph. Style change applies on next launch."
                    "gamenative" -> "GameNative-style overlay: compact pill or stacked list with live graphs. Style change applies on next launch."
                    "fusion" -> "Fusion overlay: one color-coded look in 5 sizes with percentile lows, VRAM + a Mega everything-view. Tap the HUD to cycle size."
                    else -> "Classic Bannerlator overlay."
                },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 4.dp)
            )
            if (fusion) {
                HudChipRow("Size", listOf("Full", "Tiles", "Pill", "Minimal", "Mega"), fusionSizes.indexOf(fusionSize).coerceAtLeast(0)) { fusionSize = fusionSizes[it]; apply() }
            }
        }

        // Build the currently-VISIBLE chips first (respecting per-style gating), then chunk into an
        // aligned 3-wide grid — so hidden chips never leave holes. Each stays an independent toggle.
        // For Fusion, show only the chips the SELECTED SIZE draws (FusionSize.supportedChips() — the same
        // single source of truth the view uses). Other styles keep their existing gating. Hiding a chip is
        // UI-only; buildConfig() still emits every key (strip-invariant), so toggles keep their state.
        fun show(label: String, styleOk: Boolean): Boolean = if (fusion) label in fusionChips else styleOk
        val metricChips = buildList<Triple<String, Boolean, () -> Unit>> {
            if (show("FPS", true)) add(Triple("FPS", showFPS) { showFPS = !showFPS; apply() })
            if (show("FPS graph", rich)) add(Triple("FPS graph", showGraph) { showGraph = !showGraph; apply() })
            if (show("CPU", true)) add(Triple("CPU", showCPU) { showCPU = !showCPU; apply() })
            if (!fusion && gameNative) add(Triple("CPU graph", showCpuGraph) { showCpuGraph = !showCpuGraph; apply() })
            if (show("GPU", true)) add(Triple("GPU", showGPU) { showGPU = !showGPU; apply() })
            if (!fusion && gameNative) add(Triple("GPU graph", showGpuGraph) { showGpuGraph = !showGpuGraph; apply() })
            if (show("VRAM", false)) add(Triple("VRAM", showVram) { showVram = !showVram; apply() })
            if (show("RAM", true)) add(Triple("RAM", showRAM) { showRAM = !showRAM; apply() })
            if (show("Power", true)) add(Triple("Power", showPower) { showPower = !showPower; apply() })
            if (show("Temp", true)) add(Triple("Temp", showTemp) { showTemp = !showTemp; apply() })
            if (show("GPU temp", gameNative)) add(Triple("GPU temp", showGpuTemp) { showGpuTemp = !showGpuTemp; apply() })
            if (show("Battery", gameNative)) add(Triple("Battery", showBattery) { showBattery = !showBattery; apply() })
            if (!fusion && gameNative) add(Triple("Runtime", showRuntime) { showRuntime = !showRuntime; apply() })
            if (show("0.01% low", false)) add(Triple("0.01% low", showLow001) { showLow001 = !showLow001; apply() })
            if (show("FPS .1", false)) add(Triple("FPS .1", fpsDecimal) { fpsDecimal = !fpsDecimal; apply() })
            // Fusion Mega-only metrics
            if (show("Per-core", false)) add(Triple("Per-core", showPerCore) { showPerCore = !showPerCore; apply() })
            if (show("Swap", false)) add(Triple("Swap", showSwap) { showSwap = !showSwap; apply() })
            if (show("Network", false)) add(Triple("Network", showNet) { showNet = !showNet; apply() })
            if (show("Resolution", false)) add(Triple("Resolution", showResolution) { showResolution = !showResolution; apply() })
            if (show("Proton", false)) add(Triple("Proton", showProton) { showProton = !showProton; apply() })
            if (show("Wrapper", false)) add(Triple("Wrapper", showWrapper) { showWrapper = !showWrapper; apply() })
            if (show("DX ver", false)) add(Triple("DX ver", showDxVer) { showDxVer = !showDxVer; apply() })
            if (show("Session", false)) add(Triple("Session", showSession) { showSession = !showSession; apply() })
            // Clock: gamenative's own chip, and every Fusion size (subtle corner readout)
            if (show("Clock", gameNative)) add(Triple("Clock", showClock) { showClock = !showClock; apply() })
            if (show("Engine", true)) add(Triple("Engine", showEngine) { showEngine = !showEngine; apply() })
            if (show("GPU model", rich)) add(Triple("GPU model", showGpuModel) { showGpuModel = !showGpuModel; apply() })
            if (!fusion && gameHub) add(Triple("Dual battery", dualBattery) { dualBattery = !dualBattery; apply() })
            // Global appearance control, shown for every style/size.
            add(Triple("Lock in place", hudLocked) { hudLocked = !hudLocked; apply() })
        }

        CollapsibleSection("Metrics", lead = "${metricChips.count { it.second }} on") {
            ModeChipGrid(metricChips, perRow = 3)
        }

        CollapsibleSection("Appearance") {
            LabeledSlider("HUD Scale", scaleValue, 50f..150f, { scaleValue = it }, { apply() }, format = { "${it.toInt()}%" })
            if (rich) LabeledSlider("HUD Opacity", opacityValue, 0f..100f, { opacityValue = it }, { apply() }, format = { "${it.toInt()}%" })
            else LabeledSlider("HUD Transparency", transValue, 0f..50f, { transValue = it }, { apply() }, format = { "${it.toInt()}" })
            if (gameHub) {
                HudChipRow("HUD skin", listOf("Classic", "Neon", "Mono"), skins.indexOf(skin)) { skin = skins[it]; apply() }
                HudChipRow("HUD color", listOf("Soft", "Mid", "Vivid"), colors.indexOf(color)) { color = colors[it]; apply() }
                LabeledSlider("HUD outline", outlineValue, 0f..100f, { outlineValue = it }, { apply() }, format = { "${it.toInt()}" })
                HudChipRow("Outline color", listOf("Gray", "Accent"), if (outlineAccent) 1 else 0) { outlineAccent = it == 1; apply() }
            } else if (gameNative || fusion) {
                HudChipRow("HUD color", listOf("Soft", "Mid", "Vivid"), colors.indexOf(color)) { color = colors[it]; apply() }
                LabeledSlider("HUD outline", outlineValue, 0f..100f, { outlineValue = it }, { apply() }, format = { "${it.toInt()}" })
                HudChipRow("Outline color", listOf("Gray", "Accent"), if (outlineAccent) 1 else 0) { outlineAccent = it == 1; apply() }
            }
        }

        // ── Temperature display ── only worth a section when a temperature is actually on screen.
        if (showTemp || ((gameNative || fusion) && (showGpuTemp || showBattery))) {
            CollapsibleSection("Alerts & Temp") {
                HudChipRow("Temp unit", listOf("°C", "°F"), if (tempUnitF) 1 else 0) {
                    tempUnitF = it == 1; apply()
                }
                val bandMode = if (!tempBands) 0 else if (tempAuto) 1 else 2
                HudChipRow("Danger colors", listOf("Off", "Auto", "Manual"), bandMode) {
                    tempBands = it != 0
                    tempAuto = it != 2
                    apply()
                }
                Text(
                    when (bandMode) {
                        0 -> "Temperatures use their normal color."
                        1 -> "Thresholds read from your device's own thermal trip points, falling back to safe defaults."
                        else -> "Set the red point per sensor; amber sits just below it."
                    },
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 4.dp)
                )
                if (bandMode == 2) {
                    // Only the red point is exposed; amber is derived. Nobody knows their preferred amber
                    // in the abstract, and three sliders beat six. Values are always °C.
                    LabeledSlider("CPU red at", tempRedCpu, 50f..110f, { tempRedCpu = it }, { apply() }, format = { "${it.toInt()}°C" })
                    if ((gameNative || fusion) && showGpuTemp)
                        LabeledSlider("GPU red at", tempRedGpu, 50f..110f, { tempRedGpu = it }, { apply() }, format = { "${it.toInt()}°C" })
                    LabeledSlider("Battery red at", tempRedBat, 35f..60f, { tempRedBat = it }, { apply() }, format = { "${it.toInt()}°C" })
                }
            }
        }

        CollapsibleSection("Tools") {
            // General HUD action (every style): export a device sensor report silently to Downloads, so
            // an owner can report which sysfs nodes their SoC actually exposes for any metric showing "—".
            val diagContext = LocalContext.current
            OutlinedButton(onClick = { exportHudDiagnostics(diagContext) }, modifier = Modifier.fillMaxWidth()) {
                Text("Export HUD diagnostics")
            }
            Text(
                "Saves a sensor report (CPU/GPU/temp/VRAM…) straight to your Downloads folder.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ───── Accordion group label (uppercase, letter-spaced, dim) ─────

@Composable
private fun HudGroupLabel(text: String) {
    Text(
        text.uppercase(),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 2.dp, top = 18.dp, bottom = 2.dp)
    )
}

// ───── Collapsible accordion section: top divider + clickable header (title + optional pill lead +
//       rotating chevron) + AnimatedVisibility body. Expanded state is remembered per-title. ─────

@Composable
private fun CollapsibleSection(
    title: String,
    lead: String? = null,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }
    val chevronRotation by animateFloatAsState(if (expanded) 90f else 0f, label = "hudSectionChevron")
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 14.dp, horizontal = 2.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (lead != null) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .padding(horizontal = 9.dp, vertical = 2.dp)
                ) {
                    Text(lead, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
            Spacer(Modifier.weight(1f))
            // Text chevron rotated 0°→90° on expand (no icon dependency); ">" points right when closed.
            Text(
                "›",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.rotate(chevronRotation)
            )
        }
        AnimatedVisibility(expanded) {
            Column(modifier = Modifier.padding(bottom = 6.dp)) { content() }
        }
    }
}

// ───── Live Present Mode selector (Vulkan host renderer only) ─────
// Reflects the EFFECTIVE mode: while frame generation is multiplying the activity forces Mailbox
// (effectivePresentMode()), so the Mailbox chip lights up on its own. The chips stay fully interactive
// during FG, but FIFO/Immediate taps are BLOCKED (Mailbox is required for FG's extra presents) and flash
// a transient note for ~2s instead of switching — the user's saved preference is untouched, so the
// highlight snaps back when FG turns off.
@Composable
internal fun PresentModeSection(state: XServerDrawerState) {
    val rendererIsVulkan by state.rendererIsVulkan.collectAsState()
    if (!rendererIsVulkan) return

    val presentMode by state.presentMode.collectAsState()
    val locked by state.presentModeLocked.collectAsState()

    val modes = listOf("fifo", "mailbox", "immediate")
    val labels = listOf(
        stringResource(R.string.renderer_present_mode_fifo),
        stringResource(R.string.renderer_present_mode_mailbox),
        stringResource(R.string.renderer_present_mode_immediate)
    )
    val selectedIdx = modes.indexOf(presentMode).coerceAtLeast(0)

    // Transient "blocked" note shown when the user taps FIFO/Immediate while FG forces Mailbox. Each
    // rejected tap bumps blockedFlash; the LaunchedEffect shows the note and auto-hides it after ~2s.
    var blockedFlash by remember { mutableStateOf(0) }
    var showBlocked by remember { mutableStateOf(false) }
    LaunchedEffect(blockedFlash) {
        if (blockedFlash > 0) { showBlocked = true; delay(2000); showBlocked = false }
    }

    HudChipRow(
        label = stringResource(R.string.renderer_present_mode),
        options = labels,
        selected = selectedIdx,
        onSelect = { idx ->
            val mode = modes[idx]
            if (locked) {
                // FG is multiplying -> Mailbox is forced. Tapping Mailbox is a harmless no-op; FIFO /
                // Immediate are rejected WITHOUT touching the saved mode (so it reverts on FG off).
                if (mode != "mailbox") blockedFlash++
            } else {
                state.onPresentModeChange?.accept(mode)
            }
        }
    )
    AnimatedVisibility(visible = showBlocked, enter = fadeIn(), exit = fadeOut()) {
        Text(
            stringResource(R.string.present_mode_fg_locked),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )
    }
}

// ───── 3-stop chip selector (skin / color / outline) ─────
@Composable
private fun HudChipRow(label: String, options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { idx, opt ->
                val sel = idx == selected
                // Selected = accent fill / black text, matching the scaling + frame-gen buttons.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = if (idx < options.lastIndex) 6.dp else 0.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sel) accent else MaterialTheme.colorScheme.surface)
                        .clickable { onSelect(idx) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        opt,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (sel) Color.Black else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}


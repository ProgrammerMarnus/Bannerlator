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
import com.winlator.star.ui.screens.drawer.HudTab
import com.winlator.star.ui.screens.drawer.PresentModeSection
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
                TabType.HUD -> HudTab(state)
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

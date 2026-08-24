package com.winlator.star.ui.screens.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winlator.star.R
import com.winlator.star.ui.XServerDialogState
import com.winlator.star.ui.screens.MenuItemDivider
import com.winlator.star.ui.screens.outlinedMenuCard
import com.winlator.star.ui.theme.LocalAccentDim
import kotlin.math.roundToInt

/**
 * Workstream B - shared drawer primitives, extracted verbatim from XServerDrawer.kt.
 *
 * These small composables are used by nearly every drawer tab (Graphics, HUD, ReShade,
 * Controls, Advanced, Task Manager, TV). They are relocations only - zero behavior change.
 * They intentionally carry NO reference to XServerDrawerState or any other drawer file, so
 * a tab can be extracted without dragging along the rest of the monolith.
 *
 * The two off-accent track colors are declared locally (same literal values as the drawer)
 * rather than imported, so this file stays self-contained.
 * Used by the drawer via thin aliases in XServerDrawer.kt (see Advance/Controls call sites).
 */
private val ToggleTrackOff = Color(0xFF333333)
private val ToggleThumbOff = Color(0xFF666666)

// ───── Section Header ─────

@Composable
internal fun DrawerSectionHeader(title: String) {
    val accent = MaterialTheme.colorScheme.primary
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.1f))),
                    RoundedCornerShape(1.dp)
                )
        )
    }
}

// ───── Modern Toggle Row ─────

@Composable
internal fun DrawerToggleRow(label: String, checked: Boolean, enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val accentDim = LocalAccentDim.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .then(if (enabled) Modifier.clickable { onCheckedChange(!checked) } else Modifier.alpha(0.4f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = accent,
                checkedTrackColor = accentDim,
                uncheckedThumbColor = ToggleThumbOff,
                uncheckedTrackColor = ToggleTrackOff,
            )
        )
    }
}

// ───── Modern Slider Row ─────

@Composable
internal fun DrawerLabeledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    steps: Int = 0,
    enabled: Boolean = true,
    format: (Float) -> String = { "%.0f".format(it) }
) {
    val accent = MaterialTheme.colorScheme.primary
    Column(modifier = Modifier.padding(vertical = 4.dp).then(if (enabled) Modifier else Modifier.alpha(0.4f))) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = format(value),
                style = MaterialTheme.typography.bodySmall,
                color = accent,
                fontWeight = FontWeight.Medium
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished ?: {},
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = ToggleTrackOff,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ───── Modern Accent Button ─────

@Composable
internal fun DrawerAccentButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val accentDim = LocalAccentDim.current
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(42.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = accentDim,
            contentColor = Color.White
        )
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
// ───── String formatters (extracted from XServerDrawer.kt, display-only) ─────

/** Prettify a raw dxwrapper id (e.g. \"dxvk+vkd3d\") to its display form (\"DXVK+VKD3D\"). */
internal fun DrawerPrettyDxWrapper(raw: String): String {
    if (raw.isBlank() || raw == "—") return raw
    return raw.split("+").joinToString("+") { token ->
        when (token.trim().lowercase()) {
            "dxvk" -> "DXVK"
            "vkd3d" -> "VKD3D"
            "wined3d" -> "WineD3D"
            "vegas" -> "VEGAS"
            else -> token.trim().uppercase()
        }
    }
}

/** Prettify a raw renderer id (e.g. \"vulkan\") to its display form (\"Vulkan\"). */
internal fun DrawerPrettyRenderer(raw: String): String = when (raw.trim().lowercase()) {
    "" -> raw
    "vulkan" -> "Vulkan"
    "gl", "opengl", "gles" -> "OpenGL"
    "vortek" -> "Vortek"
    else -> raw.trim().replaceFirstChar { it.uppercase() }
}

/** Tidy the device line: \"8 cores\" -> \"8c\" so it fits the narrow value column. */
internal fun DrawerTidyDevice(raw: String): String = raw.replace(" cores", "c")

// ───── Refresh-rate snap slider ─────

@Composable
internal fun DrawerRefreshRateSlider(rates: List<Int>, selected: Int, enabled: Boolean, autoRate: Int, onSelect: (Int) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val stops = remember(rates) { listOf(0) + rates }
    var idx by remember(selected, stops) { mutableStateOf(stops.indexOf(selected).coerceAtLeast(0)) }
    val dim = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    val showAuto = !enabled && autoRate > 0
    val rightText = when {
        showAuto -> "$autoRate Hz"
        stops[idx] == 0 -> "Off"
        else -> "${stops[idx]} Hz"
    }
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Rate", style = MaterialTheme.typography.bodySmall, color = if (enabled) MaterialTheme.colorScheme.onSurface else dim)
            Text(
                rightText,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled || showAuto) accent else dim,
                fontWeight = FontWeight.Medium
            )
        }
        Slider(
            value = idx.toFloat(),
            onValueChange = { idx = it.roundToInt().coerceIn(stops.indices) },
            onValueChangeFinished = { onSelect(stops[idx]) },
            valueRange = 0f..(stops.size - 1).toFloat(),
            steps = (stops.size - 2).coerceAtLeast(0),
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stops.forEach { s ->
                Text(
                    if (s == 0) "Off" else "$s",
                    fontSize = 10.sp,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else dim
                )
            }
        }
    }
}

// ───── Integer snap slider ─────

@Composable
internal fun DrawerIntSlider(label: String, value: Int, valueRange: IntRange, onValueChange: (Int) -> Unit, onValueChangeFinished: (() -> Unit)? = null, steps: Int = -1, enabled: Boolean = true) {
    val accent = MaterialTheme.colorScheme.primary
    // steps < 0 -> continuous (one stop per integer); steps >= 0 -> snap to that many
    // interior stops (e.g. steps = 3 over 0..100 yields the 5 positions {0,25,50,75,100}).
    val sliderSteps = if (steps >= 0) steps else (valueRange.last - valueRange.first - 1)
    Column(modifier = Modifier.padding(vertical = 4.dp).then(if (enabled) Modifier else Modifier.alpha(0.4f))) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "$value", style = MaterialTheme.typography.bodySmall, color = accent, fontWeight = FontWeight.Medium)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            onValueChangeFinished = { onValueChangeFinished?.invoke() },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = sliderSteps,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ───── Screen-effect shader toggle (checkbox) ─────

@Composable
internal fun DrawerSeShaderToggle(label: String, checked: Boolean, enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .then(if (enabled) Modifier.clickable { onCheckedChange(!checked) } else Modifier.alpha(0.4f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = accent,
                uncheckedColor = ToggleThumbOff,
                checkmarkColor = Color.White
            )
        )
        Spacer(Modifier.width(4.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
    }
}
// ───── Chip grids (ModeChipGrid / ToggleChipGrid) + one-chip item ─────
// Shared by the Graphics, HUD and Controls tabs. Extracted verbatim from XServerDrawer.kt.

/**
 * One on/off chip in a [DrawerToggleChipGrid]. Same tuple ToggleRow takes (label + checked +
 * enabled + callback), just laid out as a chip instead of a full-width switch row.
 */
internal data class DrawerToggleChipItem(
    val label: String,
    val checked: Boolean,
    val enabled: Boolean = true,
    val onToggle: (Boolean) -> Unit
)

@Composable
internal fun DrawerModeChipGrid(
    items: List<Triple<String, Boolean, () -> Unit>>,
    perRow: Int,
    enabled: Boolean = true,
    disabledIndices: Set<Int> = emptySet(),
) {
    val accent = MaterialTheme.colorScheme.primary
    val accentDim = LocalAccentDim.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.withIndex().chunked(perRow).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { (index, item) ->
                    val (label, isOn, onTap) = item
                    val chipEnabled = enabled && index !in disabledIndices
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isOn && chipEnabled) accent else Color.Black)
                            .border(
                                width = 1.dp,
                                color = if (isOn && chipEnabled) accent else accentDim,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = chipEnabled) { onTap() }
                            .padding(vertical = 9.dp)
                    ) {
                        Text(
                            label,
                            color = when {
                                !chipEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                isOn         -> Color.Black
                                else         -> accent
                            },
                            fontSize = 12.sp,
                            fontWeight = if (isOn && chipEnabled) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
                // Pad short final rows so every chip keeps the same width (grid stays aligned).
                repeat(perRow - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

// Compact stand-in for a run of ToggleRows: same chip language as [DrawerModeChipGrid] above
// (accent fill + bold black text ON; black bg + accentDim 1dp border + accent medium text OFF,
// equal widths, short rows padded with Spacer), but every chip toggles independently. Packing
// adjacent toggles 2-4 per row is where the vertical space comes back - a Switch row costs ~4x the
// height of a chip. Disabled chips keep ToggleRow's alpha-0.4 grey-out and swallow taps.
@Composable
internal fun DrawerToggleChipGrid(items: List<DrawerToggleChipItem>, perRow: Int = 3) {
    val accent = MaterialTheme.colorScheme.primary
    val accentDim = LocalAccentDim.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.chunked(perRow).forEach { row ->
            // IntrinsicSize.Min + fillMaxHeight keeps a row level when one label wraps to two lines.
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Split the padding for a short final row across both sides so it sits CENTRED,
                // and every chip in the grid keeps the identical width (no odd-sized leftovers).
                val missing = perRow - row.size
                val leading = missing / 2
                repeat(leading) { Spacer(Modifier.weight(1f)) }
                row.forEach { item ->
                    val isOn = item.checked && item.enabled
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isOn) accent else Color.Black)
                            .border(
                                width = 1.dp,
                                color = if (isOn) accent else accentDim,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .then(
                                if (item.enabled) Modifier.clickable { item.onToggle(!item.checked) }
                                else Modifier.alpha(0.4f)
                            )
                            .padding(horizontal = 6.dp, vertical = 9.dp)
                    ) {
                        Text(
                            item.label,
                            color = if (isOn) Color.Black else accent,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = if (isOn) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
                repeat(missing - leading) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
// ───── Mode-button rows shared by Graphics + TV (relocated from XServerDrawer.kt) ─────

// Terminal debanding (TPDF dither) — kills 8-bit gradient banding. Drawer-only / session-live.
// Shared by the GL and Vulkan graphics blocks. Reads/writes the single _debandEnabled/_debandStrength
// state and fires onDebandApply; only one renderer block is shown per session, so the
// shared state never conflicts. strength 0..200 (CPU maps /100 to LSBs, default 100 = 1 LSB).
@Composable
internal fun DrawerDebandControls(enabled: Boolean = true) {
    val initDebandEnabled  by XServerDialogState.debandEnabled.collectAsState()
    val initDebandStrength by XServerDialogState.debandStrength.collectAsState()
    var debandEnabled  by remember(initDebandEnabled)  { mutableStateOf(initDebandEnabled) }
    var debandStrength by remember(initDebandStrength) { mutableIntStateOf(initDebandStrength) }
    DrawerToggleRow("Debanding", debandEnabled, enabled) {
        debandEnabled = it
        XServerDialogState.onDebandApply?.invoke(debandEnabled, debandStrength)
    }
    if (debandEnabled) {
        Spacer(Modifier.height(4.dp))
        DrawerIntSlider("Dither strength", debandStrength, 0..200,
            onValueChange = { debandStrength = it },
            onValueChangeFinished = {
                XServerDialogState.onDebandApply?.invoke(debandEnabled, debandStrength)
            },
            enabled = enabled)
    }
}

// Fullscreen aspect-ratio selector (#71 Stage 2): 5 mode chips laid out as rows (3 + 2), same
// box-chip idiom as DrawerUpscalerModeButtons. Selecting a mode applies it live and does NOT close the
// drawer, so the user can flip between modes and settle on one before dismissing.
@Composable
internal fun DrawerFullscreenModeButtons(selected: Int, onSelect: (Int) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val accentDim = LocalAccentDim.current
    val options = listOf(
        0 to stringResource(R.string.fullscreen_mode_off_short),
        1 to stringResource(R.string.fullscreen_mode_fit_short),
        2 to stringResource(R.string.fullscreen_mode_stretch_short),
        3 to stringResource(R.string.fullscreen_mode_fill_short),
        4 to stringResource(R.string.fullscreen_mode_integer_short)
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { (mode, label) ->
                    val isSel = selected == mode
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) accent else Color.Black)
                            .border(
                                width = 1.dp,
                                color = if (isSel) accent else accentDim,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelect(mode) }
                            .padding(vertical = 9.dp)
                    ) {
                        Text(
                            label,
                            color = if (isSel) Color.Black else accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold.takeIf { isSel } ?: FontWeight.Medium
                        )
                    }
                }
                // Pad the short (2-chip) row so its buttons keep the same width as the 3-chip row.
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
internal fun DrawerUpscalerModeButtons(selected: Int, enabled: Boolean, onSelect: (Int) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val accentDim = LocalAccentDim.current
    val options = listOf(
        0 to "None", 1 to "Linear", 2 to "Nearest",
        3 to "SGSR", 4 to "FSR", 5 to "FSR (Fit)", 6 to "Sharpen", 7 to "NIS"
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { (mode, label) ->
                    val isSel = selected == mode
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel && enabled) accent else Color.Black)
                            .border(
                                width = 1.dp,
                                color = if (isSel && enabled) accent else accentDim,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = enabled) { onSelect(mode) }
                            .padding(vertical = 9.dp)
                    ) {
                        Text(
                            label,
                            color = when {
                                !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                isSel    -> Color.Black
                                else     -> accent
                            },
                            fontSize = 12.sp,
                            fontWeight = if (isSel && enabled) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
}
// ───── Frame Generation section (shared by GraphicsTab + TV; relocated from XServerDrawer.kt) ─────
// On/off is per-container; multiplier & flow scale are tuned live here and hot-reload
// via conf.toml. Multiplier is a segmented button row; the Flow Scale slider collapses
// while Off and expands when a multiplier is selected.
@Composable
internal fun DrawerFrameGenSection(state: XServerDrawerState) {
    val accent = MaterialTheme.colorScheme.primary
    val frameGenEnabled by state.frameGenEnabled.collectAsState()
    val initFgMult by state.frameGenMultiplier.collectAsState()
    val initFgFlow by state.frameGenFlowScale.collectAsState()
    val initFgModel by state.frameGenModel.collectAsState()
    val engine by state.frameGenEngine.collectAsState()
    val layerActive by state.bionicFgActive.collectAsState()
    val initLsfgPerf by state.lsfgPerformanceMode.collectAsState()

    // Title on the left, engine badge on the right (green dot = engine actually running this
    // session). Badge shows bionic-fg / lsfg-vk depending on the container's selection.
    val engineLabel = when (engine) {
        "lsfg"   -> "lsfg-vk"
        "bionic" -> "win-fg"
        else     -> "Off"
    }
    // Green dot = engine actually multiplying frames right now. Frame gen starts at multiplier 0
    // (Off) every launch even when the container has an engine selected, so gate on initFgMult too
    // — otherwise the dot would show green while FG is idle. Tracks live as the user toggles Off/2×/…
    val isRunning = layerActive && engine != "off" && initFgMult > 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Frame Generation", color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1A1A1A))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isRunning) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f))
            )
            Spacer(Modifier.width(5.dp))
            Text(
                engineLabel,
                color = if (isRunning) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
    Spacer(Modifier.height(8.dp))

    if (frameGenEnabled) {
        var fgMult by remember(initFgMult) { mutableIntStateOf(initFgMult) }
        var fgFlow by remember(initFgFlow) { mutableFloatStateOf(initFgFlow) }
        var fgModel by remember(initFgModel) { mutableIntStateOf(initFgModel) }
        fun applyFg() {
            state.setFrameGenMultiplier(fgMult)
            state.setFrameGenFlowScale(fgFlow)
            state.setFrameGenModel(fgModel)
            state.onBionicFgConfigChange?.run()
        }

        DrawerFgMultiplierButtons(fgMult, engine) { newMult ->
            val wasOff = fgMult == 0
            fgMult = newMult; applyFg()
            // Turning FG on: pulse a bg/fg reset so win-fg starts clean, not artifacty.
            if (wasOff && newMult >= 2) state.onFgResetPulse?.run()
        }

        // Interpolation model, win-fg only. The layer rebuilds its framegen context when the
        // model changes (same path as a multiplier change), so this switches live. Hidden while
        // frame gen is Off, where it would have nothing to act on.
        AnimatedVisibility(
            visible = engine == "bionic" && fgMult > 0,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Model",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )
                DrawerFgModelButtons(fgModel) { newModel ->
                    fgModel = newModel; applyFg()
                    // Model switch while FG is on -> same bg/fg reset pulse.
                    if (fgMult >= 2) state.onFgResetPulse?.run()
                }
            }
        }

        // Flow Scale only matters with frame gen actually on -> collapse it while Off.
        AnimatedVisibility(
            visible = fgMult > 0,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(Modifier.height(8.dp))
                DrawerLabeledSlider(
                    "Flow Scale", fgFlow, 0.2f..1.0f,
                    { fgFlow = it }, { applyFg() },
                    format = { "%.2f".format(it) }
                )
                Text(
                    "Higher flow scale = smoother motion estimate, more GPU cost.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }

        // lsfg-vk only: performance_mode (bionic-fg has no such setting).
        if (engine == "lsfg") {
            var lsfgPerf by remember(initLsfgPerf) { mutableStateOf(initLsfgPerf) }
            Spacer(Modifier.height(8.dp))
            DrawerToggleRow("Performance mode", lsfgPerf) {
                lsfgPerf = it
                state.setLsfgPerformanceMode(it)
                applyFg()
            }
            Text(
                "Lower quality for higher FPS — helps on low-end devices.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    } else {
        Text(
            "Enable Frame Generation in this container's settings to tune it here.",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )
    }
}

// Off / 2× / 3× / 4× segmented button row. mult values 0/2/3/4; selected = filled accent.
@Composable
internal fun DrawerFgModelButtons(selected: Int, onSelect: (Int) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val accentDim = LocalAccentDim.current
    // win-fg's two optical-flow models: 3 = single-direction flow, 4 = block-grid
    // bidirectional flow with occlusion gating (softer at occlusion edges). Legacy
    // stored values 0-2 map to the standard flow (model 3), matching the layer's clamp.
    val options = listOf(3 to "Optical flow", 4 to "Bidirectional")
    val sel = if (selected < 3) 3 else selected
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { (model, label) ->
            val isSel = sel == model
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSel) accent else Color.Black)
                    .border(
                        width = 1.dp,
                        color = if (isSel) accent else accentDim,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(model) }
                    .padding(vertical = 9.dp)
            ) {
                Text(
                    label,
                    color = if (isSel) Color.Black else accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
internal fun DrawerFgMultiplierButtons(selected: Int, engine: String, onSelect: (Int) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val accentDim = LocalAccentDim.current
    // win-fg is a simple Off / On toggle for now (On = 2×); selecting On reveals the
    // model + flow-scale controls (gated on multiplier > 0). lsfg-vk keeps 2×/3×/4×.
    val options = if (engine == "bionic")
        listOf(0 to "Off", 2 to "On")
    else
        listOf(0 to "Off", 2 to "2×", 3 to "3×", 4 to "4×")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { (mult, label) ->
            val isSel = selected == mult
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSel) accent else Color.Black)
                    .border(
                        width = 1.dp,
                        color = if (isSel) accent else accentDim,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(mult) }
                    .padding(vertical = 9.dp)
            ) {
                Text(
                    label,
                    color = if (isSel) Color.Black else accent,
                    fontSize = 13.sp,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// COMBO/RADIO/LIST dropdown shared by the ReShade effect params and the TV tab pickers — shows the
// ui_items labels and reports the selected index. Relocated from XServerDrawer.kt.
@Composable
internal fun DrawerReshadeDropdown(label: String, options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    options.getOrElse(selected) { options.firstOrNull() ?: "" },
                    color = accent, fontWeight = FontWeight.Medium, fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.outlinedMenuCard()
            ) {
                options.forEachIndexed { i, opt ->
                    if (i > 0) MenuItemDivider()
                    DropdownMenuItem(text = { Text(opt) }, onClick = { onSelect(i); expanded = false })
                }
            }
        }
    }
}

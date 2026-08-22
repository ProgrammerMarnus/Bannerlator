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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
}
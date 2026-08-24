package com.winlator.star.ui.screens.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winlator.star.reshade.ReshadeLoadout
import com.winlator.star.reshade.ReshadeManager
import com.winlator.star.ui.XServerDialogState
import com.winlator.star.ui.XServerDrawerState
import kotlin.math.roundToInt

/**
 * Workstream B - ReShade tab, extracted verbatim from XServerDrawer.kt (ReshadeContent cluster).
 *
 * Tier-1 multi-effect LOADOUT editor: master on/off, Solo/Stack mode switch, live-preview toggle,
 * and one expandable row per effect with typed controls (BOOL toggle, COMBO dropdown, COLOR HSV
 * picker, FLOAT/INT slider) plus per-effect Reset. Every change rides the single onReshadeApply seam.
 *
 * The COMBO dropdown primitive is shared with the TV tab via DrawerComponents.DrawerReshadeDropdown.
 */
@Composable
internal fun ReshadeTab(state: XServerDrawerState) {
    DrawerSectionHeader("ReShade")
    ReshadeSection()

// Tier 1 multi-effect LOADOUT — the effects picked pre-launch, switchable LIVE here. A master
// on/off (whole chain) + a Solo/Stack mode switch + one row per effect: an activation control (radio
// in solo, checkbox in stack) and an expander revealing that effect's typed controls (BOOL -> toggle,
// COMBO/RADIO/LIST -> dropdown, COLOR (floatN) -> HSV picker, FLOAT/INT -> slider) + a per-effect
// Reset. In solo mode, activating one deactivates the others. Shows a placeholder on non-DXVK/VKD3D
// games or with an empty loadout. Effect SELECTION is pre-launch (shortcut/container editor); this
// toggles + tunes the loaded set. Every change rides the single onReshadeApply seam (-> applyReshadeLive:
// conf rewrite the patched libvkbasalt mtime-watch picks up live, and persists to Container/shortcut).
@Composable
private fun ReshadeSection() {
    val accent = MaterialTheme.colorScheme.primary
    val supported by XServerDialogState.reshadeSupported.collectAsState()
    // Seed ONCE from the flows (the launch/last-applied state). Read via .value (not collectAsState)
    // so writing the snapshot back on each apply — for reopen consistency — doesn't re-key the live
    // edit state and collapse the open row. The section is recomposed fresh whenever the tab reopens.
    val seed = remember { XServerDialogState.reshadeLoadout.value }

    if (!supported || seed.isEmpty()) {
        Text(
            "No ReShade effects selected. Add one or more in this game's settings (or the container's) to switch and tune them here.",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
        )
        return
    }

    var master by remember { mutableStateOf(XServerDialogState.reshadeMasterEnabled.value) }
    var mode by remember { mutableStateOf(XServerDialogState.reshadeMode.value) }
    val enabledState = remember { mutableStateMapOf<String, Boolean>().apply { seed.forEach { put(it.name, it.enabled) } } }
    // One SnapshotStateMap of live values per effect (keyed by effect name).
    val valueState = remember { seed.associate { it.name to mutableStateMapOf<String, Float>().apply { putAll(it.values) } } }
    // Bumped on any per-effect Reset so COLOR pickers (internal HSV state) re-seed from `values`.
    var resetNonce by remember { mutableIntStateOf(0) }
    val colorSeed = remember(resetNonce) { Any() }
    // Which effect row is expanded to reveal its params (default: the first).
    var expanded by remember { mutableStateOf(seed.firstOrNull()?.name) }

    fun snapshot(): List<ReshadeLoadoutItem> = seed.map { item ->
        item.copy(
            enabled = enabledState[item.name] ?: item.enabled,
            values = valueState[item.name]?.toMap() ?: item.values
        )
    }
    fun apply() {
        val snap = snapshot()
        XServerDialogState.setReshadeMasterEnabled(master)
        XServerDialogState.setReshadeMode(mode)
        XServerDialogState.setReshadeLoadout(snap)
        XServerDialogState.onReshadeApply?.invoke(master, mode, snap)
    }
    fun setEnabled(name: String, on: Boolean) {
        if (mode == ReshadeLoadout.MODE_SOLO && on) {
            // Solo: activating one deactivates the rest.
            enabledState.keys.toList().forEach { enabledState[it] = (it == name) }
        } else {
            enabledState[name] = on
        }
        apply()
    }
    fun setMode(newMode: String) {
        mode = newMode
        // Switching to solo: keep only the first enabled effect active.
        if (newMode == ReshadeLoadout.MODE_SOLO) {
            var seen = false
            seed.forEach { item ->
                val on = enabledState[item.name] ?: false
                if (on && !seen) seen = true else enabledState[item.name] = false
            }
        }
        apply()
    }

    // "Live preview" — persisted global toggle. ON = changes apply live while the game runs. OFF
    // (default) = freeze-frame + pulse preview (first change SIGSTOPs; each later change briefly
    // resumes 1–2 frames to reveal it, then re-freezes). The activity owns the flag + the freeze/
    // pulse; this just reports it. Independent of `master` so it can be set before enabling ReShade.
    var livePreview by remember { mutableStateOf(XServerDialogState.reshadeLivePreview.value) }

    DrawerToggleRow("ReShade", master, true) { master = it; apply() }

    DrawerToggleRow("Live preview", livePreview, true) {
        livePreview = it
        XServerDialogState.setReshadeLivePreview(it)
        XServerDialogState.onReshadeLivePreviewChange?.invoke(it)
    }
    Text(
        if (livePreview) "Changes apply live; the game keeps running."
        else "Game freezes while tuning; each change pulses briefly to preview, then re-freezes.",
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp)
    )

    if (master) {
        ReshadeModeSelector(mode) { setMode(it) }
        seed.forEach { item ->
            val itemEnabled = enabledState[item.name] ?: item.enabled
            val isOpen = expanded == item.name
            // Activation control + label + expander.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (mode == ReshadeLoadout.MODE_SOLO) {
                    RadioButton(
                        selected = itemEnabled,
                        onClick = { setEnabled(item.name, true) },
                        colors = RadioButtonDefaults.colors(selectedColor = accent)
                    )
                } else {
                    Checkbox(
                        checked = itemEnabled,
                        onCheckedChange = { setEnabled(item.name, it) },
                        colors = CheckboxDefaults.colors(checkedColor = accent)
                    )
                }
                Text(
                    item.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = if (itemEnabled) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f).clickable { expanded = if (isOpen) null else item.name }
                )
                IconButton(onClick = { expanded = if (isOpen) null else item.name }) {
                    Icon(
                        if (isOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isOpen) {
                val values = valueState[item.name] ?: mutableStateMapOf()
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        values.clear()
                        item.params.forEach { p -> ReshadeManager.seedValues(p, null, values) }
                        resetNonce++
                        apply()
                    }, enabled = item.params.isNotEmpty()) {
                        Text("Reset", color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (item.params.isEmpty()) {
                    Text(
                        "No tunable parameters.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                } else {
                    ReshadeEffectParams(item.params, values, colorSeed) { apply() }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

// Solo/Stack mode switch — two pills. Solo = one effect active (A/B); Stack = layered subset.
@Composable
private fun ReshadeModeSelector(mode: String, onChange: (String) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(ReshadeLoadout.MODE_SOLO to "Solo", ReshadeLoadout.MODE_STACK to "Stack").forEach { (value, label) ->
            val selected = mode == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface)
                    .border(1.dp, if (selected) accent else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .clickable { onChange(value) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (selected) accent else MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
    Text(
        if (mode == ReshadeLoadout.MODE_SOLO) "One effect at a time (A/B compare)."
        else "Layer any subset of effects.",
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

// The typed controls for ONE effect (reused by each expanded loadout row). BOOL -> toggle,
// COMBO/RADIO/LIST -> dropdown, COLOR (floatN) -> HSV picker, FLOAT/INT (slider|drag) -> slider.
@Composable
private fun ReshadeEffectParams(
    params: List<ReshadeManager.ReshadeParam>,
    values: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Float>,
    colorSeed: Any,
    onApply: () -> Unit,
) {
    params.forEach { p ->
        when (p.type) {
            ReshadeManager.ParamType.BOOL -> {
                val v = values[p.name] ?: p.defaultValue
                Spacer(Modifier.height(4.dp))
                DrawerToggleRow(p.label, v >= 0.5f, true) {
                    values[p.name] = if (it) 1f else 0f; onApply()
                }
            }
            ReshadeManager.ParamType.COMBO -> {
                val idx = (values[p.name] ?: p.defaultValue).roundToInt()
                DrawerReshadeDropdown(p.label, p.options ?: emptyList(), idx) { sel ->
                    values[p.name] = sel.toFloat(); onApply()
                }
            }
            ReshadeManager.ParamType.COLOR -> {
                ReshadeColorControl(
                    label = p.label,
                    components = p.components,
                    seedKey = colorSeed,
                    component = { c -> values["${p.name}_$c"] ?: p.componentDefaults?.getOrNull(c) ?: 0f },
                    onChange = { comps ->
                        comps.forEachIndexed { c, value -> values["${p.name}_$c"] = value }
                        onApply()
                    }
                )
            }
            else -> {
                val v = (values[p.name] ?: p.defaultValue).coerceIn(p.min, p.max)
                Spacer(Modifier.height(4.dp))
                DrawerLabeledSlider(
                    p.label, v, p.min..p.max,
                    { values[p.name] = it },
                    { onApply() },
                    format = {
                        if (p.type == ReshadeManager.ParamType.INT) it.toInt().toString()
                        else "%.2f".format(it)
                    }
                )
            }
        }
    }
}

// COLOR (float3/float4) — full HSV color picker: preview swatch + hue/saturation/brightness (and
// alpha for float4) gradient sliders. Emits the RGB(A) components as 0..1 floats. Internal HSV state
// is keyed on [seedKey] so a Reset (or fresh seed) snaps the widget back to the resolved values.
@Composable
private fun ReshadeColorControl(
    label: String,
    components: Int,
    seedKey: Any,
    component: (Int) -> Float,
    onChange: (FloatArray) -> Unit
) {
    val initHsv = remember(seedKey) {
        val r = (component(0).coerceIn(0f, 1f) * 255f).roundToInt()
        val g = (component(1).coerceIn(0f, 1f) * 255f).roundToInt()
        val b = (component(2).coerceIn(0f, 1f) * 255f).roundToInt()
        FloatArray(3).also { android.graphics.Color.colorToHSV(android.graphics.Color.rgb(r, g, b), it) }
    }
    var hue by remember(seedKey) { mutableFloatStateOf(initHsv[0]) }
    var sat by remember(seedKey) { mutableFloatStateOf(initHsv[1]) }
    var valv by remember(seedKey) { mutableFloatStateOf(initHsv[2]) }
    var alpha by remember(seedKey) { mutableFloatStateOf(if (components >= 4) component(3).coerceIn(0f, 1f) else 1f) }

    fun rgbInt() = android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, valv))
    fun emit() {
        val c = rgbInt()
        val out = FloatArray(components)
        if (components >= 1) out[0] = android.graphics.Color.red(c) / 255f
        if (components >= 2) out[1] = android.graphics.Color.green(c) / 255f
        if (components >= 3) out[2] = android.graphics.Color.blue(c) / 255f
        if (components >= 4) out[3] = alpha
        onChange(out)
    }

    // Collapsed by default — a deep shader (e.g. Technicolor) has several color params, and
    // expanding every Hue/Sat/Brightness set at once is a wall of rainbow sliders. The header row
    // (label + swatch + chevron) is tappable to reveal this param's sliders.
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(vertical = 2.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(rgbInt()))
                    .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(6.dp))
            )
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        if (expanded) {
            GradientSlider(
                "Hue", hue, 0f..360f,
                Brush.horizontalGradient((0..12).map { Color(android.graphics.Color.HSVToColor(floatArrayOf(it * 30f, 1f, 1f))) }),
                { hue = it; emit() }
            )
            GradientSlider(
                "Saturation", sat, 0f..1f,
                Brush.horizontalGradient(listOf(
                    Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 0f, valv))),
                    Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, valv)))
                )),
                { sat = it; emit() }
            )
            GradientSlider(
                "Brightness", valv, 0f..1f,
                Brush.horizontalGradient(listOf(Color.Black, Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, 1f))))),
                { valv = it; emit() }
            )
            if (components >= 4) {
                GradientSlider(
                    "Alpha", alpha, 0f..1f,
                    Brush.horizontalGradient(listOf(Color.Black, Color(rgbInt()))),
                    { alpha = it; emit() }
                )
            }
        }
    }
}

// Drawer-dark track colors, declared locally so this file stays self-contained
// (same literals as the monolith / DrawerComponents).
private val PureBlack = Color(0xFF000000)
private val ToggleTrackOff = Color(0xFF333333)

// Tappable/draggable gradient track slider (drawer dark idiom) used by the color picker.
@Composable
private fun GradientSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    track: Brush,
    onValueChange: (Float) -> Unit
) {
    var width by remember { mutableIntStateOf(0) }
    fun pick(x: Float) {
        if (width > 0) {
            val frac = (x / width).coerceIn(0f, 1f)
            onValueChange(range.start + frac * (range.endInclusive - range.start))
        }
    }
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(track)
                .border(1.dp, ToggleTrackOff, RoundedCornerShape(11.dp))
                .onSizeChanged { width = it.width }
                .pointerInput(range) { detectTapGestures { pick(it.x) } }
                .pointerInput(range) { detectHorizontalDragGestures { ch, _ -> pick(ch.position.x) } }
        ) {
            val frac = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
            Box(Modifier.fillMaxSize().padding(horizontal = 3.dp), contentAlignment = Alignment.CenterStart) {
                Box(Modifier.fillMaxWidth(frac)) {
                    Box(
                        Modifier
                            .align(Alignment.CenterEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, PureBlack, CircleShape)
                    )
                }
            }
        }
    }
}
}
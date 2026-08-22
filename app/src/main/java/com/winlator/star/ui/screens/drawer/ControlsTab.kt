package com.winlator.star.ui.screens.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import com.winlator.star.R
import com.winlator.star.ui.XServerDialogState
import com.winlator.star.ui.XServerDrawerState
import com.winlator.star.ui.components.ColorPicker
import com.winlator.star.ui.screens.MenuItemDivider
import com.winlator.star.ui.screens.outlinedMenuCard
import com.winlator.star.ui.theme.LocalAccentDim

/**
 * Workstream B - Controls tab, extracted verbatim from XServerDrawer.kt (ControlsContent cluster).
 *
 * Self-contained per-tab file: binds only to XServerDialogState (profiles / vibration / gyro /
 * player-slot flows + action seams) and XServerDrawerState (cursor-to-touch, mouse, overlay, accent,
 * gestures), plus the shared Drawer* primitives in this package. No coupling to other drawer tabs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ControlsTab(state: XServerDrawerState) {
    val accent = MaterialTheme.colorScheme.primary
    val profiles by XServerDialogState.inputProfiles.collectAsState()
    val initProfileIdx by XServerDialogState.selectedProfileIdx.collectAsState()
    val initTouchscreen by XServerDialogState.showTouchscreen.collectAsState()
    val initTimeout by XServerDialogState.timeoutEnabled.collectAsState()
    val initHaptics by XServerDialogState.hapticsEnabled.collectAsState()

    val moveCursorToTouch by state.moveCursorToTouchpoint.collectAsState()
    val isRelativeMouse by state.isRelativeMouseMovement.collectAsState()
    val isMouseDisabled by state.isMouseDisabled.collectAsState()
    val initOverlayOpacity by state.overlayOpacity.collectAsState()
    val controlsFollowTheme by state.controlsFollowTheme.collectAsState()
    val initControlsAccent by state.controlsAccentColor.collectAsState()

    DrawerSectionHeader("Controls")

    // Four unrelated feature areas live under this tab, so they're segmented rather than stacked —
    // exactly one renders at a time. ModeChipGrid is already the drawer's segmented-control language
    // (equal-width accent-filled chips), so the bar reads as native here instead of a new widget.
    val subTab by state.controlsSubTab.collectAsState()
    DrawerModeChipGrid(
        listOf(
            Triple("Touch", subTab == 0) { state.setControlsSubTab(0) },
            Triple("Mouse", subTab == 1) { state.setControlsSubTab(1) },
            Triple("Vibration", subTab == 2) { state.setControlsSubTab(2) },
            Triple("Gyro", subTab == 3) { state.setControlsSubTab(3) },
            Triple("Players", subTab == 4) { state.setControlsSubTab(4) },
        ),
        perRow = 3
    )
    Spacer(Modifier.height(8.dp))

    // Input Controls section — hoisted above the sub-tab switch so the in-flight profile/flag edits
    // survive a hop to another sub-tab and back.
    var selectedIdx by remember(initProfileIdx) { mutableIntStateOf(initProfileIdx) }
    var showTouchscreen by remember(initTouchscreen) { mutableStateOf(initTouchscreen) }
    var timeoutEnabled by remember(initTimeout) { mutableStateOf(initTimeout) }
    var hapticsEnabled by remember(initHaptics) { mutableStateOf(initHaptics) }
    val allItems = listOf("-- Disabled --") + profiles
    var dropdownExpanded by remember { mutableStateOf(false) }

    when (subTab) {
        // ── Touch ──
        0 -> {
            Text("Input Controls", color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))

            ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = it }) {
                OutlinedTextField(
                    value = allItems.getOrElse(selectedIdx) { "-- Disabled --" },
                    onValueChange = {}, readOnly = true,
                    label = { Text("Profile", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true,
                )
                ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                    allItems.forEachIndexed { i, label ->
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            selectedIdx = i
                            dropdownExpanded = false
                            XServerDialogState.onInputControlsConfirm?.invoke(selectedIdx, showTouchscreen, timeoutEnabled, hapticsEnabled)
                        })
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            // Three plain on/off flags that all round-trip through the same onInputControlsConfirm call —
            // packed as chips rather than full-width switch rows to keep the sub-tab short.
            DrawerToggleChipGrid(
                listOf(
                    DrawerToggleChipItem("Touch Controls", showTouchscreen) {
                        showTouchscreen = it
                        XServerDialogState.onInputControlsConfirm?.invoke(selectedIdx, showTouchscreen, timeoutEnabled, hapticsEnabled)
                    },
                    DrawerToggleChipItem("Timeout", timeoutEnabled) {
                        timeoutEnabled = it
                        XServerDialogState.onInputControlsConfirm?.invoke(selectedIdx, showTouchscreen, timeoutEnabled, hapticsEnabled)
                    },
                    DrawerToggleChipItem("Haptics", hapticsEnabled) {
                        hapticsEnabled = it
                        XServerDialogState.onInputControlsConfirm?.invoke(selectedIdx, showTouchscreen, timeoutEnabled, hapticsEnabled)
                    },
                ),
                perRow = 3
            )

            // On-screen controls opacity — live, applied to the visible overlay as you drag.
            var overlayOpacity by remember(initOverlayOpacity) { mutableFloatStateOf(initOverlayOpacity) }
            DrawerLabeledSlider(
                label = "Overlay Opacity",
                value = overlayOpacity,
                valueRange = 0f..1f,
                onValueChange = {
                    overlayOpacity = it
                    state.setOverlayOpacity(it)
                    state.onOverlayOpacityChange?.run()
                },
                format = { "${(it * 100).toInt()}%" },
            )

            // On-screen controls accent — per-profile override. Follow the app theme (default) or pick a
            // custom accent for the active profile; idle controls stay white, pressed auto-brightens.
            Spacer(Modifier.height(4.dp))
            DrawerToggleChipGrid(
                listOf(
                    DrawerToggleChipItem("App Theme", controlsFollowTheme) {
                        state.setControlsFollowTheme(it)
                        state.onControlsColorChange?.run()
                    }
                ),
                perRow = 3
            )
            if (!controlsFollowTheme) {
                Spacer(Modifier.height(8.dp))
                Text("Controls Accent", color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                ColorPicker(
                    initialColor = Color(initControlsAccent),
                    onColorChanged = {
                        state.setControlsAccentColor(it.toArgb())
                        state.onControlsColorChange?.run()
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    XServerDialogState.onInputControlsConfirm?.invoke(selectedIdx, showTouchscreen, timeoutEnabled, hapticsEnabled)
                    XServerDialogState.onInputControlsSettings?.invoke(selectedIdx)
                },
                enabled = selectedIdx > 0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
            ) { Text("Profile Settings\u2026") }

            Spacer(Modifier.height(4.dp))

            DrawerAccentButton("Apply & Close") {
                XServerDialogState.onInputControlsConfirm?.invoke(selectedIdx, showTouchscreen, timeoutEnabled, hapticsEnabled)
                state.onClose?.run()
                Unit
            }
        }

        // ── Mouse ──
        1 -> {
            Text("Mouse & Cursor", color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))

            DrawerToggleChipGrid(
                listOf(
                    DrawerToggleChipItem("Cursor to Touch", moveCursorToTouch) {
                        state.onMoveCursorToTouchpoint?.run()
                    },
                    DrawerToggleChipItem("Relative Mouse", isRelativeMouse) {
                        state.onRelativeMouseMovement?.run()
                    },
                    DrawerToggleChipItem("Disable Mouse", isMouseDisabled) {
                        state.onDisableMouse?.run()
                    },
                ),
                perRow = 3
            )

            if (moveCursorToTouch) TouchGestureSettings(state)
        }
        // ── Vibration ──
        2 -> {
            Text("Vibration", color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))

            // Master kill-switch — off suppresses ALL controller rumble regardless of slot (and hides the
            // rumble target, intensity, and per-slot rows below, which are moot while it's off). Persists
            // globally.
            val vibrationMasterOn by XServerDialogState.vibrationMasterEnabled.collectAsState()
            DrawerToggleChipGrid(
                listOf(
                    DrawerToggleChipItem("Enabled", vibrationMasterOn) {
                        XServerDialogState.setVibrationMasterEnabled(it)
                        XServerDialogState.onVibrationMasterChanged?.invoke(it)
                    }
                ),
                perRow = 3
            )
            if (vibrationMasterOn) {
                // Per-container rumble target + intensity (PC-accurate dual-motor rumble). Keyed on the
                // incoming config so re-opening the drawer doesn't drift from a stale capture — same pattern
                // as the lsfg "Performance mode" toggle elsewhere in this drawer.
                val initVibrationMode by XServerDialogState.vibrationMode.collectAsState()
                var vibrationMode by remember(initVibrationMode) { mutableIntStateOf(initVibrationMode) }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Rumble Target",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                VibrationModeButtons(vibrationMode) {
                    vibrationMode = it
                    XServerDialogState.setVibrationMode(it)
                    XServerDialogState.onVibrationModeChanged?.invoke(it)
                }

                if (vibrationMode != 0) {
                    val initVibrationIntensity by XServerDialogState.vibrationIntensity.collectAsState()
                    var vibrationIntensity by remember(initVibrationIntensity) { mutableIntStateOf(initVibrationIntensity) }
                    DrawerIntSlider("Intensity", vibrationIntensity, 0..100,
                        onValueChange = { vibrationIntensity = it },
                        onValueChangeFinished = {
                            XServerDialogState.setVibrationIntensity(vibrationIntensity)
                            XServerDialogState.onVibrationIntensityChanged?.invoke(vibrationIntensity)
                        }
                    )
                }
            }
            val vibrationSlots by XServerDialogState.vibrationSlots.collectAsState()
            if (vibrationMasterOn && vibrationSlots.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                DrawerToggleChipGrid(
                    vibrationSlots.mapIndexed { index, slot ->
                        DrawerToggleChipItem(slot.first, slot.second) {
                            XServerDialogState.updateVibrationSlot(index, it)          // reflect in the UI immediately
                            XServerDialogState.onVibrationSlotChanged?.invoke(index, it) // persist to WinHandler
                        }
                    },
                    perRow = 3
                )
            }
        }

        // ── Gyro ── its own branch, deliberately OUTSIDE the vibration block above: the gyro section
        // is unrelated to rumble and must render whether or not vibration is switched on.
        3 -> GyroSection()

        // ── Players ── manual per-device slot assignment (override when auto-assignment guesses wrong).
        4 -> PlayersSection()
    }
}

// Per-container rumble target picker (Off/Controller/Device/Both) — same segmented-chip style as
// UpscalerModeButtons, just a fixed 4-wide row. "Device" = the phone's own vibrator; "Both" drives
// the physical controller AND the phone together.
@Composable
private fun VibrationModeButtons(selected: Int, enabled: Boolean = true, onSelect: (Int) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val accentDim = LocalAccentDim.current
    val options = listOf(0 to "Off", 1 to "Controller", 2 to "Device", 3 to "Both")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { (mode, label) ->
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
                    fontSize = 11.sp,
                    fontWeight = if (isSel && enabled) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// ───── Touch gesture settings — Controls > Mouse, shown while Cursor to Touch is on ─────
// These gestures only exist in absolute-cursor mode, so the pane lives and dies with that toggle
// rather than behind its own control. Each gesture is independently switchable because the right set
// is per-game: an RTS wants both, a mouse-look shooter wants neither stealing its drags. Changes
// apply to the live touchpad immediately and persist, so a game can be tuned without relaunching.
// There is no pinch-to-zoom — two-finger pan already emits the same wheel events.
@Composable
private fun TouchGestureSettings(state: XServerDrawerState) {
    val accent = MaterialTheme.colorScheme.primary
    val dragSelect by state.gestureDragSelect.collectAsState()
    val longPress by state.gestureLongPressRightClick.collectAsState()

    Spacer(Modifier.height(10.dp))
    Text("Touch Gestures", color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    Spacer(Modifier.height(2.dp))
    Text(
        "Drag to box-select, hold for right click. Two-finger drag scrolls the wheel.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(6.dp))

    DrawerToggleChipGrid(
        listOf(
            DrawerToggleChipItem("Box Select", dragSelect) {
                state.setGestureDragSelect(it); state.onGestureConfigChange?.run()
            },
            DrawerToggleChipItem("Hold = Right", longPress) {
                state.setGestureLongPressRightClick(it); state.onGestureConfigChange?.run()
            },
        ),
        perRow = 2
    )

    // The slider only exists while its gesture does — a hold delay with holds switched off is the
    // kind of dead control the sub-tab split was meant to get rid of.
    if (longPress) {
        val initHoldMs by state.gestureLongPressMs.collectAsState()
        var holdMs by remember(initHoldMs) { mutableIntStateOf(initHoldMs) }
        DrawerIntSlider("Hold Delay", holdMs, 150..800,
            onValueChange = { holdMs = it },
            onValueChangeFinished = {
                state.setGestureLongPressMs(holdMs); state.onGestureConfigChange?.run()
            }
        )
    }
}

// ───── Gyro (motion aim) section — Controls tab, "Gyro" sub-tab ─────
// Progressive disclosure: the master chip is always visible; everything downstream only appears once
// the gyro is on, so the tab isn't a wall of dead controls. Order is by how often a control is
// touched — Enable, target, sensitivity, activation, then the set-once fine tuning. Hidden entirely
// on devices with no gyroscope.
@Composable
private fun GyroSection() {
    val accent = MaterialTheme.colorScheme.primary
    val gyroSupported by XServerDialogState.gyroSupported.collectAsState()
    if (!gyroSupported) return

    Text(stringResource(R.string.gyro_drawer_title), color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    Spacer(Modifier.height(4.dp))

    val gyroEnabled by XServerDialogState.gyroEnabled.collectAsState()
    DrawerToggleChipGrid(
        listOf(
            DrawerToggleChipItem(stringResource(R.string.gyro_drawer_enabled), gyroEnabled) {
                XServerDialogState.setGyroEnabled(it)
                XServerDialogState.onGyroEnabledChanged?.invoke(it)
            }
        ),
        perRow = 3
    )
    if (!gyroEnabled) return

    // Where the tilt goes. Right/Left stick overlay the gamepad; Mouse drives the pointer instead,
    // which is the only target that does anything on a Wine desktop or in a mouse-look game.
    val initGyroTarget by XServerDialogState.gyroTarget.collectAsState()
    var gyroTarget by remember(initGyroTarget) { mutableIntStateOf(initGyroTarget) }

    // How the tilt is READ. Rate = the tilt speed drives the stick and it recentres when you stop;
    // Tilt to aim = the stick follows the angle you hold, so a held tilt keeps aiming. The two are
    // mutually exclusive with the Mouse target (a held tilt would be a constant pointer delta and
    // the pointer would run to a screen edge), so each greys the other's chip out with a reason.
    val orientationSupported by XServerDialogState.gyroOrientationSupported.collectAsState()
    val initGyroMode by XServerDialogState.gyroMode.collectAsState()
    var gyroMode by remember(initGyroMode) { mutableIntStateOf(initGyroMode) }
    val orientationBlockedByMouse = gyroTarget == 2
    val orientationSelectable = orientationSupported && !orientationBlockedByMouse
    Spacer(Modifier.height(6.dp))
    Text(stringResource(R.string.gyro_drawer_mode), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(4.dp))
    DrawerModeChipGrid(
        listOf(
            Triple(stringResource(R.string.gyro_drawer_mode_rate), gyroMode == 0) { setGyroModeLive(0) { gyroMode = it } },
            Triple(stringResource(R.string.gyro_drawer_mode_orientation), gyroMode == 1) { setGyroModeLive(1) { gyroMode = it } },
        ),
        perRow = 2,
        disabledIndices = if (orientationSelectable) emptySet() else setOf(1)
    )
    if (!orientationSupported) {
        GyroHint(stringResource(R.string.gyro_drawer_orientation_unsupported))
    }
    else if (orientationBlockedByMouse) {
        GyroHint(stringResource(R.string.gyro_drawer_orientation_mouse_hint))
    }
    else if (gyroMode == 1) {
        GyroHint(stringResource(R.string.gyro_drawer_orientation_hint))
        Spacer(Modifier.height(6.dp))
        DrawerAccentButton(stringResource(R.string.gyro_drawer_recenter)) {
            XServerDialogState.onGyroRecenterRequested?.invoke()
        }
        GyroHint(stringResource(R.string.gyro_drawer_recenter_hint))
    }
    Spacer(Modifier.height(6.dp))
    Text(stringResource(R.string.gyro_drawer_apply_to), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(4.dp))
    DrawerModeChipGrid(
        listOf(
            Triple(stringResource(R.string.gyro_drawer_target_right_stick), gyroTarget == 0) { setGyroTargetLive(0) { gyroTarget = it } },
            Triple(stringResource(R.string.gyro_drawer_target_left_stick), gyroTarget == 1) { setGyroTargetLive(1) { gyroTarget = it } },
            Triple(stringResource(R.string.gyro_drawer_target_mouse), gyroTarget == 2) { setGyroTargetLive(2) { gyroTarget = it } },
        ),
        perRow = 3,
        disabledIndices = if (gyroMode == 1) setOf(2) else emptySet()
    )
    if (gyroMode == 1) {
        GyroHint(stringResource(R.string.gyro_drawer_mouse_unavailable_hint))
    }
    else if (gyroTarget == 2) {
        GyroHint(stringResource(R.string.gyro_drawer_mouse_hint))
    }

    // Sensitivity is the one knob people reach for constantly, so it sits right under the target.
    val initGyroSensitivity by XServerDialogState.gyroSensitivity.collectAsState()
    var gyroSensitivity by remember(initGyroSensitivity) { mutableFloatStateOf(initGyroSensitivity) }
    DrawerLabeledSlider(
        label = stringResource(R.string.gyro_sensitivity_label),
        value = gyroSensitivity,
        valueRange = 0.1f..10f,
        onValueChange = { gyroSensitivity = it },
        onValueChangeFinished = {
            XServerDialogState.setGyroSensitivity(gyroSensitivity)
            XServerDialogState.onGyroSensitivityChanged?.invoke(gyroSensitivity)
        },
        format = { "%.1f".format(it) }
    )

    // Which button gates the tilt. "Always on" removes the gate entirely.
    val initGyroActivator by XServerDialogState.gyroActivator.collectAsState()
    var gyroActivator by remember(initGyroActivator) { mutableIntStateOf(initGyroActivator) }
    Spacer(Modifier.height(2.dp))
    Text(stringResource(R.string.gyro_drawer_activation), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(4.dp))
    DrawerModeChipGrid(
        listOf(
            Triple(stringResource(R.string.gyro_activator_l1), gyroActivator == 0) { setGyroActivatorLive(0) { gyroActivator = it } },
            Triple(stringResource(R.string.gyro_activator_l2), gyroActivator == 1) { setGyroActivatorLive(1) { gyroActivator = it } },
            Triple(stringResource(R.string.gyro_activator_r1), gyroActivator == 2) { setGyroActivatorLive(2) { gyroActivator = it } },
            Triple(stringResource(R.string.gyro_activator_r3), gyroActivator == 3) { setGyroActivatorLive(3) { gyroActivator = it } },
            Triple(stringResource(R.string.gyro_drawer_activator_always), gyroActivator == 4) { setGyroActivatorLive(4) { gyroActivator = it } },
        ),
        perRow = 5
    )

    // Hold vs Toggle for that button. Greyed rather than hidden under "Always".
    val initGyroActivationMode by XServerDialogState.gyroActivationMode.collectAsState()
    var gyroActivationMode by remember(initGyroActivationMode) { mutableIntStateOf(initGyroActivationMode) }
    val activationModeEnabled = gyroActivator != 4
    Spacer(Modifier.height(4.dp))
    DrawerModeChipGrid(
        listOf(
            Triple(stringResource(R.string.gyro_activation_hold), gyroActivationMode == 0) {
                setGyroActivationModeLive(0) { gyroActivationMode = it }
            },
            Triple(stringResource(R.string.gyro_activation_toggle), gyroActivationMode == 1) {
                setGyroActivationModeLive(1) { gyroActivationMode = it }
            },
        ),
        perRow = 2,
        enabled = activationModeEnabled
    )
    if (activationModeEnabled && gyroActivationMode == 1) {
        GyroHint(stringResource(R.string.gyro_drawer_toggle_hint))
    }

    // ---- Fine tuning: set once, then forgotten. ----
    val initGyroDeadzone by XServerDialogState.gyroDeadzone.collectAsState()
    var gyroDeadzone by remember(initGyroDeadzone) { mutableFloatStateOf(initGyroDeadzone) }
    DrawerLabeledSlider(
        label = stringResource(R.string.gyro_deadzone_label),
        value = gyroDeadzone,
        valueRange = 0f..0.5f,
        onValueChange = { gyroDeadzone = it },
        onValueChangeFinished = {
            XServerDialogState.setGyroDeadzone(gyroDeadzone)
            XServerDialogState.onGyroDeadzoneChanged?.invoke(gyroDeadzone)
        },
        format = { "%.2f".format(it) }
    )

    val initGyroSmoothing by XServerDialogState.gyroSmoothing.collectAsState()
    var gyroSmoothing by remember(initGyroSmoothing) { mutableFloatStateOf(initGyroSmoothing) }
    DrawerLabeledSlider(
        label = stringResource(R.string.gyro_smoothing_label),
        value = gyroSmoothing,
        valueRange = 0f..0.95f,
        onValueChange = { gyroSmoothing = it },
        onValueChangeFinished = {
            XServerDialogState.setGyroSmoothing(gyroSmoothing)
            XServerDialogState.onGyroSmoothingChanged?.invoke(gyroSmoothing)
        },
        format = { "%.2f".format(it) }
    )

    val gyroInvertX by XServerDialogState.gyroInvertX.collectAsState()
    val gyroInvertY by XServerDialogState.gyroInvertY.collectAsState()
    DrawerToggleChipGrid(
        listOf(
            DrawerToggleChipItem(stringResource(R.string.gyro_invert_x), gyroInvertX) {
                XServerDialogState.setGyroInvertX(it)
                XServerDialogState.onGyroInvertXChanged?.invoke(it)
            },
            DrawerToggleChipItem(stringResource(R.string.gyro_invert_y), gyroInvertY) {
                XServerDialogState.setGyroInvertY(it)
                XServerDialogState.onGyroInvertYChanged?.invoke(it)
            },
        ),
        perRow = 3
    )
}
// The one hint-text treatment used throughout the gyro section: dimmed, small, tucked under the row
// it explains.
@Composable
private fun GyroHint(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        fontSize = 11.sp,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
    )
}

// Target/activator changes hit the local picker state AND WinHandler (which resets the overlay).
private fun setGyroTargetLive(target: Int, reflect: (Int) -> Unit) {
    reflect(target)
    XServerDialogState.setGyroTarget(target)
    XServerDialogState.onGyroTargetChanged?.invoke(target)
}

private fun setGyroActivatorLive(activator: Int, reflect: (Int) -> Unit) {
    reflect(activator)
    XServerDialogState.setGyroActivator(activator)
    XServerDialogState.onGyroActivatorChanged?.invoke(activator)
}

// Read-mode switch — the activity re-registers the sensor on the way through.
private fun setGyroModeLive(mode: Int, reflect: (Int) -> Unit) {
    reflect(mode)
    XServerDialogState.setGyroMode(mode)
    XServerDialogState.onGyroModeChanged?.invoke(mode)
}

// Switching to Hold can't leave a latched-on gyro behind.
private fun setGyroActivationModeLive(mode: Int, reflect: (Int) -> Unit) {
    reflect(mode)
    XServerDialogState.setGyroActivationMode(mode)
    XServerDialogState.onGyroActivationModeChanged?.invoke(mode)
}
// ───── Controls > Players — manual per-device XInput slot assignment ─────
// One row per detected input device (plus the on-screen pad), each with a Player 1-4 / Ignore / Auto
// selector. Applied live (WinHandler.setDeviceSlotAssignment) and persisted per-container. Fixes the
// case where auto-assignment hands Player 1 to the wrong device (e.g. an aux media-button board that
// sorts first). The list is re-read from WinHandler each time the sub-tab opens, since devices
// hot-plug.
@Composable
private fun PlayersSection() {
    val accent = MaterialTheme.colorScheme.primary
    val rows by XServerDialogState.playerSlots.collectAsState()

    LaunchedEffect(Unit) { XServerDialogState.onPlayerSlotsRefresh?.run() }

    Text("Player Slots", color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    Spacer(Modifier.height(4.dp))
    Text(
        "Assign each device to a player, or ignore it. Applied immediately and saved for this container.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(8.dp))

    if (rows.isEmpty()) {
        Text(
            "No input devices detected.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        rows.forEachIndexed { i, row ->
            if (i > 0) HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            PlayerSlotRowItem(row)
        }
    }

    // Manual recovery: rebuild the fake-input transport in place (no relaunch).
    Spacer(Modifier.height(4.dp))
    OutlinedButton(
        onClick = {
            XServerDialogState.onResetInput?.run()
            XServerDialogState.onPlayerSlotsRefresh?.run()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
    ) { Text("Reset Input") }
    Text(
        "Re-handshake controllers & on-screen if input stops responding.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// One device row: name + "currently Player N/unassigned" subtitle + a slot selector dropdown.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerSlotRowItem(row: XServerDialogState.PlayerSlotRow) {
    val accent = MaterialTheme.colorScheme.primary

    val options = remember {
        buildList {
            add("Auto" to XServerDialogState.SLOT_AUTO)
            for (i in 0 until 4) add("Player ${i + 1}" to i)
            add("Ignore" to XServerDialogState.SLOT_IGNORE)
        }
    }
    val selectedLabel = options.firstOrNull { it.second == row.override }?.first ?: "Auto"

    val subtitle = when {
        row.currentSlot >= 0 -> "Currently Player ${row.currentSlot + 1}"
        row.override == XServerDialogState.SLOT_IGNORE -> "Ignored"
        else -> "Unassigned"
    }

    var expanded by remember(row.descriptor, row.override) { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        Text(
            row.displayName,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            subtitle + if (row.isOnScreen) " · on-screen controls" else "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {}, readOnly = true,
                label = { Text("Slot", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                singleLine = true,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.outlinedMenuCard()
            ) {
                options.forEachIndexed { index, option ->
                    if (index > 0) MenuItemDivider()
                    val (label, value) = option
                    DropdownMenuItem(text = { Text(label) }, onClick = {
                        expanded = false
                        if (value != row.override) {
                            XServerDialogState.onPlayerSlotChanged?.invoke(row.descriptor, value)
                        }
                    })
                }
            }
        }
    }
}

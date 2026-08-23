package com.winlator.star.ui.screens.drawer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winlator.star.ui.XServerDrawerState
import com.winlator.star.ui.components.AudioSettingsDialog
import com.winlator.star.ui.components.loadAudioConfig
import com.winlator.star.ui.components.saveAudioConfig

/**
 * Workstream B - Audio tab, extracted verbatim from XServerDrawer.kt (AudioContent).
 *
 * In-game audio: adaptive presets + fine-tuning, applied LIVE via onReapplyAudio (sink recreate).
 * Guest-buffer latency is fixed at connect, so that one knob is flagged "next launch" in the dialog.
 *
 * The heavy lifting lives in ui/components/AudioSettingsDialog; this tab is just the entry point.
 */
@Composable
internal fun AudioTab(state: XServerDrawerState) {
    val ctx = LocalContext.current
    var show by remember { mutableStateOf(false) }
    val driverId = state.audioDriverId
    var cfg by remember { mutableStateOf(loadAudioConfig(ctx, driverId)) }
    Text(
        "Audio",
        fontSize = 18.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(2.dp))
    val engine = state.audioDriverLabel
    Text(
        if (engine.isNotBlank()) "Engine: $engine  ·  preset: ${cfg.preset}" else "Current preset: ${cfg.preset}",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp
    )
    Spacer(Modifier.height(12.dp))
    DrawerAccentButton("Presets & fine-tuning", Modifier.fillMaxWidth()) { show = true }
    Text(
        "Balance crackle vs delay. Applies live; guest buffer needs a relaunch.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
    Spacer(Modifier.height(12.dp))
    DrawerAccentButton("Reset audio", Modifier.fillMaxWidth()) { state.onResetAudio?.run() }
    Text(
        "Fixes lost sound after switching apps.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
    if (show) {
        AudioSettingsDialog(
            initial = cfg,
            scopeLabel = "live · this session",
            latencyLive = false,
            driverLabel = engine,
            driverId = driverId,
            onDismiss = { show = false },
            onSave = { newCfg ->
                saveAudioConfig(ctx, driverId, newCfg)
                cfg = newCfg
                state.onReapplyAudio?.run()
                show = false
            }
        )
    }
}
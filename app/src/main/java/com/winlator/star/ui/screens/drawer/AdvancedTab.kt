package com.winlator.star.ui.screens.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winlator.star.R
import com.winlator.star.perf.PerfGpuTurbo
import com.winlator.star.perf.PerfRevertRegistry
import com.winlator.star.perf.PerfRootApplier
import com.winlator.star.perf.RootManager
import com.winlator.star.ui.XServerDrawerState
import com.winlator.star.ui.screens.WatchdogSection
import kotlinx.coroutines.delay

/**
 * Workstream B - Advanced tab, extracted verbatim from XServerDrawer.kt (AdvancedContent cluster).
 *
 * Quick-launch action rows (Magnifier / Active Windows / Debug Logs / PiP / Keyboard), the non-root
 * Performance toggles with per-game override indicators, the GPU clock-lock row, the root
 * Performance tier (governor / freq / cores / thermal / fan + memory actions + temperature
 * watchdog), and the one-tap reset-all-overrides affordance.
 *
 * Binds to XServerDrawerState flows/callbacks plus the shared perf stores (RootManager.state,
 * PerfRevertRegistry.harnessProven) and PerfRootApplier keys — identical wiring to before the
 * extraction. Shared primitives come from DrawerComponents.kt in this package.
 */
@Composable
internal fun AdvancedTab(state: XServerDrawerState) {
    DrawerSectionHeader("Advanced")

    AdvancedActionRow("Magnifier", R.drawable.icon_magnifier) {
        state.onClose?.run(); state.onMagnifier?.run()
    }
    AdvancedActionRow("Active Windows", R.drawable.icon_active_windows) {
        state.onClose?.run(); state.onActiveWindows?.run()
    }
    AdvancedActionRow("Debug Logs", R.drawable.icon_debug) {
        state.onClose?.run(); state.onLogs?.run()
    }
    AdvancedActionRow("Picture-in-Picture", R.drawable.ic_picture_in_picture_alt) {
        state.onClose?.run(); state.onPipMode?.run()
    }
    AdvancedActionRow("Show Keyboard", R.drawable.icon_keyboard) {
        state.onClose?.run(); state.onKeyboard?.run()
    }

    Spacer(Modifier.height(14.dp))

    // ── Performance ── non-root power-user toggles; always enabled, applied + persisted live.
    // Each row shows whether it's a per-game override or inheriting the App Settings global default,
    // with a "Reset to global" affordance (a per-game toggle is only saved when it differs).
    DrawerSectionHeader("Performance")

    val overridden by state.overriddenKeys.collectAsState()

    val sustainedPerf by state.sustainedPerfMode.collectAsState()
    DrawerToggleRow("Sustained Performance Mode", sustainedPerf) {
        state.setSustainedPerfMode(it); state.onSustainedPerfModeChange?.run()
    }
    PerfOverrideLine("sustainedPerfMode" in overridden) { state.onResetPerfKey?.accept("sustainedPerfMode") }

    val priorityBoost by state.perfPriorityBoost.collectAsState()
    DrawerToggleRow("Thread Priority Boost", priorityBoost) {
        state.setPerfPriorityBoost(it); state.onPerfPriorityBoostChange?.run()
    }
    PerfOverrideLine("perfPriorityBoost" in overridden) { state.onResetPerfKey?.accept("perfPriorityBoost") }

    val bigCores by state.preferBigCores.collectAsState()
    DrawerToggleRow("Prefer Big Cores", bigCores) {
        state.setPreferBigCores(it); state.onPreferBigCoresChange?.run()
    }
    PerfOverrideLine("preferBigCores" in overridden) { state.onResetPerfKey?.accept("preferBigCores") }

    // GPU max-clock pin — sits with the no-root toggles because on Adreno it works without root
    // (KGSL turbo); it upgrades to the sysfs pin automatically when root is granted.
    GpuClockLockRow(state, overridden)

    Spacer(Modifier.height(14.dp))
    RootPerformanceSection(state)

    // One-tap "reset every per-game override" — shown only when this game overrides something.
    if (overridden.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        Text(
            "↺ Reset all game overrides to global",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { state.onResetAllPerf?.run() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
// Subtle per-toggle indicator: "per-game override (Reset)" vs "using global default".
@Composable
private fun PerfOverrideLine(overridden: Boolean, onReset: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 1.dp, bottom = 6.dp)
    ) {
        Text(
            if (overridden) "● Per-game override" else "○ Using global default",
            fontSize = 10.sp,
            color = if (overridden) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (overridden) {
            Text("Reset to global", fontSize = 10.sp, color = accent,
                modifier = Modifier.clickable { onReset() })
        }
    }
}

/**
 * "Lock GPU to max clock" — the one PerfRootApplier-owned toggle that is NOT root-only, so it lives
 * with the non-root rows. Enabled when root is granted (sysfs pwrlevel pin) OR the device is Adreno
 * (non-root KGSL turbo). On a non-Adreno device with no root there is nothing to drive, so the row
 * greys out with a reason.
 */
@Composable
private fun GpuClockLockRow(state: XServerDrawerState, overridden: Set<String>) {
    val key = PerfRootApplier.KEY_GPU_CLOCK_LOCK
    val rootState by RootManager.state.collectAsState()
    val toggles by state.rootToggles.collectAsState()

    val granted = rootState == RootManager.RootState.GRANTED
    val enabled = granted || PerfGpuTurbo.isSupported

    DrawerToggleRow("Lock GPU to max clock", toggles[key] ?: false, enabled = enabled) { on ->
        state.setRootToggle(key, on)
        state.onRootToggleChange?.accept(key, on)
    }
    if (!enabled) {
        Text(
            "🔒 Needs an Adreno GPU, or root on other GPUs.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
        )
    } else {
        PerfOverrideLine(key in overridden) { state.onResetPerfKey?.accept(key) }
    }
}
// ── Root Performance sub-section (in-game). Mirrors App Settings' root tier; toggles are enabled
// only when root is GRANTED. Binds to the same shared stores (RootManager.state, PerfRevertRegistry
// .harnessProven, TempWatchdog.enabled) so App Settings <-> in-game stay in sync. ──
@Composable
private fun RootPerformanceSection(state: XServerDrawerState) {
    val rootState by RootManager.state.collectAsState()
    val harnessProven by PerfRevertRegistry.harnessProven.collectAsState()
    val toggles by state.rootToggles.collectAsState()
    val readouts by state.rootReadouts.collectAsState()
    val overridden by state.overriddenKeys.collectAsState()

    val granted = rootState == RootManager.RootState.GRANTED

    // Poll the live readouts while this section is on screen.
    LaunchedEffect(granted) {
        while (true) {
            state.onRootReadoutPoll?.run()
            delay(1500)
        }
    }

    DrawerSectionHeader("Root Performance")

    if (!granted) {
        Text(
            "🔒 " + when (rootState) {
                RootManager.RootState.UNAVAILABLE -> "No root manager detected."
                else -> "Grant root in App Settings → Performance to enable these."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    } else {
        // Live readouts.
        val gov = readouts["governor"] ?: "—"
        val gpu = readouts["gpuMhz"] ?: "—"
        val temp = readouts["socTemp"] ?: "—"
        val fan = readouts["fanRpm"] ?: "—"
        Text(
            "Gov $gov · GPU $gpu · SoC $temp · Fan $fan",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }

    RootToggleRow(PerfRootApplier.KEY_CPU_GOVERNOR, "CPU governor → performance", state, toggles, granted, harnessProven, overridden)
    RootToggleRow(PerfRootApplier.KEY_CPU_FREQ_LOCK, "Lock CPU frequency to max", state, toggles, granted, harnessProven, overridden)
    RootToggleRow(PerfRootApplier.KEY_CORES_ONLINE, "Keep all cores online", state, toggles, granted, harnessProven, overridden)
    RootToggleRow(PerfRootApplier.KEY_THERMAL_DISABLE, "Disable thermal throttling", state, toggles, granted, harnessProven, overridden)
    RootToggleRow(PerfRootApplier.KEY_FAN_MAX, "Fan to maximum", state, toggles, granted, harnessProven, overridden)

    if (granted && !harnessProven) {
        Text(
            "Thermal / fan locked until safety-revert is verified on this device.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (granted) {
        Spacer(Modifier.height(6.dp))
        // TIER 1 — light, honest label. Drops file caches; the system reclaims cache automatically.
        AdvancedActionRow(
            "Drop file caches", R.drawable.icon_task_manager,
            subtitle = "Frees cached files. Little visible RAM; the system reclaims cache automatically.",
        ) { state.onFreeMemory?.run() }
        // TIER 2 — the real RAM free (root-only). `am kill-all` never touches the running game/system.
        AdvancedActionRow(
            "Deep clean (free app memory)", R.drawable.icon_task_manager,
            subtitle = "Force-closes background apps to free real memory. Won't touch your game or system.",
        ) { state.onDeepClean?.run() }
    }

    // Temperature watchdog (device-wide; shared control block, identical + synced with App Settings).
    Spacer(Modifier.height(10.dp))
    WatchdogSection()
}

@Composable
private fun RootToggleRow(
    key: String,
    label: String,
    state: XServerDrawerState,
    toggles: Map<String, Boolean>,
    granted: Boolean,
    harnessProven: Boolean,
    overridden: Set<String>,
) {
    val gated = PerfRootApplier.isHarnessGated(key) && !harnessProven
    val enabled = granted && !gated
    DrawerToggleRow(label, toggles[key] ?: false, enabled = enabled) { on ->
        state.setRootToggle(key, on)
        state.onRootToggleChange?.accept(key, on)
    }
    PerfOverrideLine(key in overridden) { state.onResetPerfKey?.accept(key) }
}

@Composable
private fun AdvancedActionRow(
    label: String,
    iconRes: Int,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
}
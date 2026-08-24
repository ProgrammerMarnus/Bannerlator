package com.winlator.star.ui.screens.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winlator.star.R
import com.winlator.star.container.Container
import com.winlator.star.ui.XServerDialogState
import com.winlator.star.ui.XServerDrawerState
import com.winlator.star.widget.exportHudDiagnostics
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/**
 * Workstream B - HUD tab, extracted verbatim from XServerDrawer.kt (HudContent cluster).
 *
 * FPS overlay configuration: master toggle, Performance accordion (FPS limiter + presets,
 * refresh-rate Auto/manual), style-specific element groups (classic / gamehub / gamenative / fusion),
 * temperature alerts, diagnostics export — plus the accordion primitives (HudGroupLabel,
 * CollapsibleSection), the live Present-Mode selector shared with GraphicsTab, and HudChipRow.
 */
// ───── HUD Tab ─────

@Composable
internal fun HudTab(state: XServerDrawerState) {
    val accent = MaterialTheme.colorScheme.primary
    val fpsConfig by state.fpsConfig.collectAsState()

    // Re-read the live display refresh rate when this tab opens so the "Rate" readout is fresh on
    // open; the display listener keeps it current while the drawer stays open.
    LaunchedEffect(Unit) { state.onRefreshRatePoll?.run() }

    DrawerSectionHeader("HUD")

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
    DrawerToggleRow("Show HUD", hudEnabled) { hudEnabled = it; apply() }

    // ── Performance group: always shown. The limiter + refresh live here regardless of the HUD. ──
    HudGroupLabel("Performance")
    CollapsibleSection("Frame rate & refresh", lead = "always on", initiallyExpanded = true) {
        Text("FPS Limiter", color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        DrawerToggleRow("Limit FPS", limiterOn) { limiterOn = it; applyLimiter() }
        if (limiterOn) {
            DrawerLabeledSlider(
                "Max FPS", limitVal.toFloat(), 10f..200f,
                { limitVal = it.roundToInt() }, { applyLimiter() },
                format = { "${it.roundToInt()}" }
            )
            // Quick presets: set the cap in one tap. Shares limitVal with the slider above, so the
            // slider thumb snaps to the picked value (and the matching chip highlights on any value).
            Spacer(Modifier.height(6.dp))
            DrawerModeChipGrid(
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
        DrawerToggleRow("Auto (match FPS)", matchRefreshOn && vrrSupported, enabled = vrrSupported) {
            matchRefreshOn = it
            state.setMatchRefreshRate(it)
            state.onMatchRefreshChange?.run()
        }
        // Manual rate slider: selectable only when Auto is OFF and the panel can switch rates.
        if (supportedRefreshRates.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            DrawerRefreshRateSlider(supportedRefreshRates, manualRefreshRate, vrrSupported && !matchRefreshOn, currentRefreshRate) { rate ->
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
            DrawerModeChipGrid(metricChips, perRow = 3)
        }

        CollapsibleSection("Appearance") {
            DrawerLabeledSlider("HUD Scale", scaleValue, 50f..150f, { scaleValue = it }, { apply() }, format = { "${it.toInt()}%" })
            if (rich) DrawerLabeledSlider("HUD Opacity", opacityValue, 0f..100f, { opacityValue = it }, { apply() }, format = { "${it.toInt()}%" })
            else DrawerLabeledSlider("HUD Transparency", transValue, 0f..50f, { transValue = it }, { apply() }, format = { "${it.toInt()}" })
            if (gameHub) {
                HudChipRow("HUD skin", listOf("Classic", "Neon", "Mono"), skins.indexOf(skin)) { skin = skins[it]; apply() }
                HudChipRow("HUD color", listOf("Soft", "Mid", "Vivid"), colors.indexOf(color)) { color = colors[it]; apply() }
                DrawerLabeledSlider("HUD outline", outlineValue, 0f..100f, { outlineValue = it }, { apply() }, format = { "${it.toInt()}" })
                HudChipRow("Outline color", listOf("Gray", "Accent"), if (outlineAccent) 1 else 0) { outlineAccent = it == 1; apply() }
            } else if (gameNative || fusion) {
                HudChipRow("HUD color", listOf("Soft", "Mid", "Vivid"), colors.indexOf(color)) { color = colors[it]; apply() }
                DrawerLabeledSlider("HUD outline", outlineValue, 0f..100f, { outlineValue = it }, { apply() }, format = { "${it.toInt()}" })
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
                    DrawerLabeledSlider("CPU red at", tempRedCpu, 50f..110f, { tempRedCpu = it }, { apply() }, format = { "${it.toInt()}°C" })
                    if ((gameNative || fusion) && showGpuTemp)
                        DrawerLabeledSlider("GPU red at", tempRedGpu, 50f..110f, { tempRedGpu = it }, { apply() }, format = { "${it.toInt()}°C" })
                    DrawerLabeledSlider("Battery red at", tempRedBat, 35f..60f, { tempRedBat = it }, { apply() }, format = { "${it.toInt()}°C" })
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


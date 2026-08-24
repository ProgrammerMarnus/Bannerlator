package com.winlator.star.ui.screens.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winlator.star.R
import com.winlator.star.ui.FexMode
import com.winlator.star.ui.XServerDialogState
import com.winlator.star.ui.XServerDrawerState

/**
 * Workstream B - Graphics tab, extracted verbatim from XServerDrawer.kt (GraphicsContent cluster).
 *
 * Renderer-aware enhancement controls: Frame Generation section, Present Mode selector, fullscreen
 * aspect modes, and per-renderer blocks (OpenGL / Vulkan / SurfaceFlinger notice) plus the Native
 * Rendering toggle and runtime-backend diagnostic chip.
 *
 * Cross-cluster notes: PresentModeSection still lives in XServerDrawer.kt (migrates with the HUD
 * extraction); FrameGen trio + Fullscreen/Upscaler/Deband helpers are shared via DrawerComponents.kt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GraphicsTab(state: XServerDrawerState) {
    val accent = MaterialTheme.colorScheme.primary
    LaunchedEffect(Unit) {
        XServerDialogState.onInitGraphicsTab?.run()
    }

    // Title on the left, runtime-backend diagnostic chip pinned top-right.
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.weight(1f)) { DrawerSectionHeader("Graphics") }
        RuntimeBackendChip(state)
    }

    // Frame Generation pinned to the top of the Graphics tab.
    DrawerFrameGenSection(state)

    // Present Mode selector (Vulkan renderer only) — directly under Frame Generation because the two
    // interact: while FG multiplies, the host present mode is forced to Mailbox (reflected live here).
    PresentModeSection(state)

    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 6.dp))

    // Fullscreen aspect-ratio mode (#71 Stage 2): a segmented selector (Off/Fit/Stretch/Fill/Integer)
    // that sets the mode live WITHOUT closing the drawer, so the user can compare modes before
    // dismissing it — same box-chip idiom as the Scaling-mode row.
    val fullscreenMode by state.fullscreenMode.collectAsState()
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.fullscreen_mode),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(R.drawable.icon_fullscreen),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp)
        )
    }
    Spacer(Modifier.height(6.dp))
    DrawerFullscreenModeButtons(selected = fullscreenMode) { state.onSetFullscreenMode?.accept(it) }

    Spacer(Modifier.height(4.dp))

    // Renderer-specific graphics controls. Each host renderer has its own set, so
    // show ONLY the set that applies to the active renderer instead of packing the
    // tab with disabled rows: GL effects on OpenGL, Scaling mode on Vulkan, and
    // nothing on SurfaceFlinger (frames are scanned out directly, bypassing the
    // compositor post-process pass). The two flags are mutually exclusive and fixed
    // for the session (the renderer is chosen at launch).
    val effectsSupported by XServerDialogState.effectsSupported.collectAsState() // OpenGL renderer
    val vulkanSupported  by XServerDialogState.vulkanSupported.collectAsState()  // Vulkan renderer

    // P5: GL Native Rendering (direct scanout) bypasses the entire EffectComposer chain + the GL
    // scaling/upscaler modes, so grey those controls out while native is on (they'd be dead toggles).
    // Reactive — flipping the Native Rendering toggle below recomposes this and updates the grey-out
    // live without reopening the drawer. (Only the GL block is gated; the Vulkan block uses its own
    // reset-on-enable mutual exclusion and stays interactive.)
    val nativeRenderingEnabled by state.nativeRenderingEnabled.collectAsState()
    val glEnabled = !nativeRenderingEnabled
    val glHeaderColor = if (glEnabled) accent else accent.copy(alpha = 0.4f)

    if (effectsSupported) {
        // ---- OpenGL: Scaling mode (real SGSR / FSR1 spatial upscalers; parity with the
        //      Vulkan picker). Modes 0/1/2 drive the base sampler filter; 3/4/5 engage the
        //      EffectComposer low-res upscale stage; 6 = the existing CAS sharpen.
        //      Drawer-only / session-live. ----
        val initGlUpscalerMode by XServerDialogState.glUpscalerMode.collectAsState()
        var glUpscalerMode by remember(initGlUpscalerMode) { mutableIntStateOf(initGlUpscalerMode) }

        Text("Scaling mode", color = glHeaderColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        DrawerUpscalerModeButtons(glUpscalerMode, glEnabled) {
            glUpscalerMode = it
            XServerDialogState.onGlUpscalerApply?.invoke(it)
        }
        // "Sharpness" drives SGSR EdgeSharpness / FSR RCAS / CAS / NIS, for the sharpening modes.
        if (glUpscalerMode == 3 || glUpscalerMode == 4 || glUpscalerMode == 5 || glUpscalerMode == 6 || glUpscalerMode == 7) {
            val initGlUpscaleSharpness by XServerDialogState.glUpscaleSharpness.collectAsState()
            var glUpscaleSharpness by remember(initGlUpscaleSharpness) { mutableIntStateOf(initGlUpscaleSharpness) }
            Spacer(Modifier.height(4.dp))
            // Continuous for SGSR/FSR (3/4/5); snapped to 5 stops {0,25,50,75,100} for
            // Sharpen mode (6), where stop 0 = OFF (no CAS pass).
            DrawerIntSlider("Sharpness", glUpscaleSharpness, 0..100,
                onValueChange = { glUpscaleSharpness = it },
                onValueChangeFinished = {
                    XServerDialogState.onGlUpscaleSharpnessApply?.invoke(glUpscaleSharpness)
                },
                steps = if (glUpscalerMode == 6) 3 else -1,
                enabled = glEnabled)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 6.dp))

        // ---- OpenGL: SGSR / HDR + Screen Effects (GL EffectComposer features) ----
        val initSgsrEnabled   by XServerDialogState.sgsrEnabled.collectAsState()
        val initSgsrSharpness by XServerDialogState.sgsrSharpness.collectAsState()
        val initHdrEnabled    by XServerDialogState.hdrEnabled.collectAsState()
        var sgsrEnabled   by remember(initSgsrEnabled)   { mutableStateOf(initSgsrEnabled) }
        var sgsrSharpness by remember(initSgsrSharpness) { mutableIntStateOf(initSgsrSharpness) }
        var hdrEnabled    by remember(initHdrEnabled)    { mutableStateOf(initHdrEnabled) }

        DrawerToggleRow("Sharpen (CAS)", sgsrEnabled, glEnabled) { sgsrEnabled = it; pushSgsrUpdate(sgsrEnabled, sgsrSharpness, hdrEnabled) }
        if (sgsrEnabled) {
            Spacer(Modifier.height(4.dp))
            // Standalone CAS sharpen: always snapped to 5 stops {0,25,50,75,100}, stop 0 = OFF.
            DrawerIntSlider("Sharpness", sgsrSharpness, 0..100,
                onValueChange = { sgsrSharpness = it },
                onValueChangeFinished = { pushSgsrUpdate(sgsrEnabled, sgsrSharpness, hdrEnabled) },
                steps = 3, enabled = glEnabled)
        }
        DrawerToggleRow("HDR", hdrEnabled, glEnabled) { hdrEnabled = it; pushSgsrUpdate(sgsrEnabled, sgsrSharpness, hdrEnabled) }

        // Terminal debanding (TPDF dither) — kills 8-bit gradient banding. Drawer-only / session-live.
        DrawerDebandControls(glEnabled)

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 6.dp))

        Text("Screen Effects", color = glHeaderColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))

        val seBrightness by XServerDialogState.seBrightness.collectAsState()
        val seContrast by XServerDialogState.seContrast.collectAsState()
        val seGamma by XServerDialogState.seGamma.collectAsState()
        val seFxaa by XServerDialogState.seFxaa.collectAsState()
        val seCrt by XServerDialogState.seCrt.collectAsState()
        val seToon by XServerDialogState.seToon.collectAsState()
        val seNtsc by XServerDialogState.seNtsc.collectAsState()
        var localBrightness by remember(seBrightness) { mutableFloatStateOf(seBrightness) }
        var localContrast by remember(seContrast) { mutableFloatStateOf(seContrast) }
        var localGamma by remember(seGamma) { mutableFloatStateOf(seGamma) }
        var localFxaa by remember(seFxaa) { mutableStateOf(seFxaa) }
        var localCrt by remember(seCrt) { mutableStateOf(seCrt) }
        var localToon by remember(seToon) { mutableStateOf(seToon) }
        var localNtsc by remember(seNtsc) { mutableStateOf(seNtsc) }

        fun applySe() {
            XServerDialogState.onScreenEffectsApply?.invoke(localBrightness, localContrast, localGamma, localFxaa, localCrt, localToon, localNtsc, 0)
        }

        DrawerLabeledSlider("Brightness", localBrightness, -100f..100f, { localBrightness = it; applySe() }, enabled = glEnabled)
        DrawerLabeledSlider("Contrast", localContrast, -100f..100f, { localContrast = it; applySe() }, enabled = glEnabled)
        DrawerLabeledSlider("Gamma", localGamma, 0.5f..3.0f, { localGamma = it; applySe() }, enabled = glEnabled, format = { "%.2f".format(it) })

        DrawerSeShaderToggle("FXAA", localFxaa, glEnabled) { localFxaa = it; applySe() }
        DrawerSeShaderToggle("CRT", localCrt, glEnabled) { localCrt = it; applySe() }
        DrawerSeShaderToggle("Toon", localToon, glEnabled) { localToon = it; applySe() }
        DrawerSeShaderToggle("NTSC", localNtsc, glEnabled) { localNtsc = it; applySe() }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 6.dp))
    }

    if (vulkanSupported) {
        // ---- Vulkan: Scaling mode (spatial upscaler) ----
        // Single source of truth for scaling/filtering on the Vulkan renderer (modes
        // 1/2 drive the base sampler filter natively). Keyed on the live value so the
        // picker reflects the seeded/launch config. Drawer-only / session-live.
        val initUpscalerMode by XServerDialogState.upscalerMode.collectAsState()
        var upscalerMode by remember(initUpscalerMode) { mutableIntStateOf(initUpscalerMode) }

        Text("Scaling mode", color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        DrawerUpscalerModeButtons(upscalerMode, true) {
            upscalerMode = it
            XServerDialogState.onUpscalerApply?.invoke(it)
        }

        // "Sharpness" controls the REAL upscaler sharpness (RCAS stops / SGSR EdgeSharpness /
        // NIS sharpness) and only applies to the sharpening scaling modes (SGSR/FSR/FSR-Fit/Sharpen/NIS).
        if (upscalerMode == 3 || upscalerMode == 4 || upscalerMode == 5 || upscalerMode == 6 || upscalerMode == 7) {
            val initUpscaleSharpness by XServerDialogState.upscaleSharpness.collectAsState()
            var upscaleSharpness by remember(initUpscaleSharpness) { mutableIntStateOf(initUpscaleSharpness) }
            Spacer(Modifier.height(4.dp))
            DrawerIntSlider("Sharpness", upscaleSharpness, 0..100, { upscaleSharpness = it }, {
                XServerDialogState.onUpscaleSharpnessApply?.invoke(upscaleSharpness)
            })
        }

        Spacer(Modifier.height(4.dp))

        // ---- Composable post effects (layer on top of any scaling mode) ----
        val initCasEnabled   by XServerDialogState.casEnabled.collectAsState()
        val initCasSharpness by XServerDialogState.casSharpness.collectAsState()
        val initHdrVkEnabled by XServerDialogState.hdrVkEnabled.collectAsState()
        var casEnabled   by remember(initCasEnabled)   { mutableStateOf(initCasEnabled) }
        var casSharpness by remember(initCasSharpness) { mutableIntStateOf(initCasSharpness) }
        var hdrVkEnabled by remember(initHdrVkEnabled) { mutableStateOf(initHdrVkEnabled) }

        DrawerToggleRow("CAS", casEnabled, true) {
            casEnabled = it
            XServerDialogState.onCasApply?.invoke(casEnabled, casSharpness)
        }
        if (casEnabled) {
            Spacer(Modifier.height(4.dp))
            DrawerIntSlider("CAS Sharpness", casSharpness, 0..100, { casSharpness = it }, {
                XServerDialogState.onCasApply?.invoke(casEnabled, casSharpness)
            })
        }
        DrawerToggleRow("HDR", hdrVkEnabled, true) {
            hdrVkEnabled = it
            XServerDialogState.onHdrApply?.invoke(hdrVkEnabled)
        }

        // Terminal debanding (TPDF dither) — kills 8-bit gradient banding. Drawer-only / session-live.
        DrawerDebandControls()

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 6.dp))

        // ---- Screen Effects (GL EffectComposer parity, ported to the Vulkan post
        //      chain). Color grade is always-applied via the sliders (neutral = no-op);
        //      FXAA/Toon/CRT/NTSC are toggles. Drawer-only / session-live. ----
        Text("Screen Effects", color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))

        val initVkBrightness by XServerDialogState.vkBrightness.collectAsState()
        val initVkContrast   by XServerDialogState.vkContrast.collectAsState()
        val initVkGamma      by XServerDialogState.vkGamma.collectAsState()
        val initVkFxaa       by XServerDialogState.vkFxaa.collectAsState()
        val initVkToon       by XServerDialogState.vkToon.collectAsState()
        val initVkCrt        by XServerDialogState.vkCrt.collectAsState()
        val initVkNtsc       by XServerDialogState.vkNtsc.collectAsState()
        var vkBrightness by remember(initVkBrightness) { mutableFloatStateOf(initVkBrightness) }
        var vkContrast   by remember(initVkContrast)   { mutableFloatStateOf(initVkContrast) }
        var vkGamma      by remember(initVkGamma)      { mutableFloatStateOf(initVkGamma) }
        var vkFxaa       by remember(initVkFxaa)       { mutableStateOf(initVkFxaa) }
        var vkToon       by remember(initVkToon)       { mutableStateOf(initVkToon) }
        var vkCrt        by remember(initVkCrt)        { mutableStateOf(initVkCrt) }
        var vkNtsc       by remember(initVkNtsc)       { mutableStateOf(initVkNtsc) }

        fun applyVkSe() {
            XServerDialogState.onVulkanScreenEffectsApply?.invoke(
                vkBrightness, vkContrast, vkGamma, vkFxaa, vkToon, vkCrt, vkNtsc)
        }

        DrawerLabeledSlider("Brightness", vkBrightness, -100f..100f, { vkBrightness = it; applyVkSe() }, enabled = true)
        DrawerLabeledSlider("Contrast", vkContrast, -100f..100f, { vkContrast = it; applyVkSe() }, enabled = true)
        DrawerLabeledSlider("Gamma", vkGamma, 0.5f..3.0f, { vkGamma = it; applyVkSe() }, enabled = true, format = { "%.2f".format(it) })

        // Four independent shader flags with identical wiring — one row of chips instead of four
        // switch rows. Same applyVkSe() round-trip as before.
        DrawerToggleChipGrid(
            listOf(
                DrawerToggleChipItem("FXAA", vkFxaa) { vkFxaa = it; applyVkSe() },
                DrawerToggleChipItem("Toon", vkToon) { vkToon = it; applyVkSe() },
                DrawerToggleChipItem("CRT", vkCrt) { vkCrt = it; applyVkSe() },
                DrawerToggleChipItem("NTSC", vkNtsc) { vkNtsc = it; applyVkSe() },
            ),
            perRow = 4
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 6.dp))
    }

    if (!effectsSupported && !vulkanSupported) {
        // ---- SurfaceFlinger: direct scanout bypasses the compositor, so no
        //      post-process / scaling controls apply. ----
        Text(
            "No graphics enhancements are available with the SurfaceFlinger renderer.",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 6.dp))
    }

    // nativeRenderingEnabled is collected once at the top of GraphicsTab (drives the GL grey-out).
    // Native Rendering is only offered on renderers that support direct scanout (Vulkan); it's hidden
    // on the OpenGL renderer, where the bespoke GL scanout path is disabled for now.
    val nativeRenderingSupported by state.nativeRenderingSupported.collectAsState()
    if (nativeRenderingSupported)
        DrawerToggleRow("Native Rendering", nativeRenderingEnabled) { state.onNativeRenderingToggle?.run() }

}

private fun pushSgsrUpdate(enabled: Boolean, sharpness: Int, hdr: Boolean) {
    XServerDialogState.onSgsrUpdate?.invoke(enabled, sharpness, hdr)
}

// ───── Runtime-backend diagnostic chip (Graphics tab header) ─────
// Read-only status: arch · translator, plus the FEX unixlib mode. unixlib (native .so loaded) =
// accent/green; DLL (self-contained FEX DLL path) = muted. Box64/x86-64 shows no unixlib segment.
// It is a status readout, never a "faster" flag. Hidden until the activity seeds arch+translator.
@Composable
private fun RuntimeBackendChip(state: XServerDrawerState) {
    val backend by state.runtimeBackend.collectAsState()
    if (!backend.isValid) return

    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            "${backend.arch} · ${backend.translator}",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        if (backend.showsFexMode) {
            val (label, color) = when (backend.fexMode) {
                FexMode.UNIXLIB -> "unixlib" to Color(0xFF4CAF50) // native .so loaded
                FexMode.DLL     -> "DLL"     to muted             // self-contained DLL path
                FexMode.NA      -> "N/A"     to muted             // maps not resolved yet
            }
            Text(" · ", color = muted, fontSize = 10.sp)
            Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

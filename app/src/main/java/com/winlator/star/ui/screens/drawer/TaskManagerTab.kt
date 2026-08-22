package com.winlator.star.ui.screens.drawer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winlator.star.R
import com.winlator.star.ui.XServerDialogState
import com.winlator.star.ui.screens.MenuItemDivider
import com.winlator.star.ui.screens.outlinedMenuCard

/**
 * Workstream B - Task Manager tab, extracted verbatim from XServerDrawer.kt (TmContent cluster).
 *
 * Self-contained: only depends on XServerDialogState (reads/writes the polled state + action
 * seams), the shared DrawerSectionHeader / DrawerPretty* helpers (same package), and its own
 * private sub-composables. No coupling to XServerDrawerState or other drawer tabs.
 */
private data class TmTile(val caption: String, val big: String, val small: String, val tint: Color)

@Composable
internal fun TaskManagerTab() {
    val accent = MaterialTheme.colorScheme.primary
    val processes by XServerDialogState.tmProcesses.collectAsState()
    val cpuCores by XServerDialogState.tmCpuCores.collectAsState()
    val cpuTitle by XServerDialogState.tmCpuTitle.collectAsState()
    val memTitle by XServerDialogState.tmMemTitle.collectAsState()
    val memInfo by XServerDialogState.tmMemInfo.collectAsState()
    val count by XServerDialogState.tmCount.collectAsState()
    val header by XServerDialogState.tmHeader.collectAsState()
    val containerInfo by XServerDialogState.tmContainerInfo.collectAsState()

    // Polling is driven by a render-independent Handler timer in XServerDisplayActivity
    // (started via onTaskManager). A Compose LaunchedEffect delay() loop here stalls on the
    // Vulkan host-render path and left the Task Manager empty. onTmRefresh kicks an immediate
    // first refresh on entry; onTmDismissed stops the Activity timer on exit.
    LaunchedEffect(Unit) {
        XServerDialogState.onTmRefresh?.run()
    }

    DisposableEffect(Unit) {
        onDispose { XServerDialogState.onTmDismissed?.run() }
    }

    DrawerSectionHeader("Task Manager")

    // Enriched header: live perf stat grid + per-core clocks + collapsible container config.
    TmStatGrid(header)
    TmCoreStrip(header)
    TmContainerPanel(containerInfo)
    Spacer(Modifier.height(8.dp))

    Text(
        text = "Processes: $count",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
    )

    Spacer(Modifier.height(6.dp))

    if (processes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No processes", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    } else {
        // Each process is its own card (matching the app File Manager rows), not a flat
        // divider-separated list; cards self-space via their vertical padding.
        Column(modifier = Modifier.fillMaxWidth()) {
            processes.forEach { proc ->
                TmProcessRow(proc)
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    Row(modifier = Modifier.fillMaxWidth()) {
        TextButton(
            onClick = {
                XServerDialogState.onTmDismissed?.run()
                XServerDialogState.onTmNewTask?.run()
            }
        ) { Text("New Task\u2026", color = accent) }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = { XServerDialogState.onTmDismissed?.run() }) { Text("Clear", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun TmProcessRow(proc: XServerDialogState.TmProcess) {
    val accent = MaterialTheme.colorScheme.primary
    var menuExpanded by remember { mutableStateOf(false) }
    var showAffinity by remember { mutableStateOf(false) }

    if (showAffinity) {
        ProcessorAffinityDialog(proc = proc, onDismiss = { showAffinity = false })
    }

    // Card per process, matching the app File Manager item style (rounded surfaceContainer
    // panel + outline border + vertical margin) instead of a flat divider-separated row.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (proc.icon != null) {
                Image(
                    bitmap = proc.icon.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.taskmgr_process),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = proc.name + if (proc.wow64) " *32" else "",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "PID ${proc.pid}  \u2022  ${proc.formattedMemory}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.outlinedMenuCard()
                ) {
                    DropdownMenuItem(
                        text = { Text("Processor Affinity") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Memory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            showAffinity = true
                        },
                    )
                    MenuItemDivider()
                    DropdownMenuItem(
                        text = { Text("Bring to Front") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.FlipToFront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            XServerDialogState.onTmBringToFront?.invoke(proc.name, proc.pid)
                        },
                    )
                    MenuItemDivider()
                    DropdownMenuItem(
                        text = { Text("End Process", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            XServerDialogState.onTmKillProcess?.invoke(proc.name)
                        },
                    )
                }
            }
        }
    }
}

/**
 * Windows Task Manager-style "Set affinity" for one running process/service. A checkbox per logical
 * CPU (plus "<All Processors>"), pre-ticked to the process's current mask, pins that PID to the
 * chosen cores live via the already-wired [XServerDialogState.onTmSetAffinity]. Not persisted — it
 * resets if the process restarts, exactly like Windows.
 */
@Composable
private fun ProcessorAffinityDialog(
    proc: XServerDialogState.TmProcess,
    onDismiss: () -> Unit,
) {
    val coreCount = remember { Runtime.getRuntime().availableProcessors().coerceIn(1, 32) }
    val allMask = remember(coreCount) { if (coreCount >= 32) -1 else (1 shl coreCount) - 1 }
    // Open pre-ticked to the cores the user actually chose. Prefer the live override cache (what the
    // user last applied) over the guest's GetProcessAffinityMask readback, which is unreliable under
    // wow64/FEX. Fall back to the guest value, then to all.
    var mask by remember(proc.pid) {
        val stored = XServerDialogState.onTmQueryAffinity?.invoke(proc.pid) ?: -1
        val m = (if (stored > 0) stored else proc.affinityMask) and allMask
        mutableStateOf(if (m == 0) allMask else m)
    }
    val allChecked = (mask and allMask) == allMask

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Memory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Processor Affinity", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    "Which processors are allowed to run \"${proc.name}\"?",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                AffinityCheckRow(
                    label = "<All Processors>",
                    checked = allChecked,
                    bold = true,
                    onToggle = { mask = if (allChecked) 0 else allMask },
                    modifier = Modifier.fillMaxWidth(),
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Box(
                    modifier = Modifier
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    CorePanel(count = coreCount) { i ->
                        val on = (mask shr i) and 1 == 1
                        val cbAccent = MaterialTheme.colorScheme.primary
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mask = (mask xor (1 shl i)) and allMask }
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                        ) {
                            Checkbox(
                                checked = on,
                                onCheckedChange = { mask = (mask xor (1 shl i)) and allMask },
                                colors = CheckboxDefaults.colors(checkedColor = cbAccent),
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(Modifier.height(2.dp))
                            Text("CPU$i", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = (mask and allMask) != 0,
                onClick = {
                    XServerDialogState.onTmSetAffinity?.invoke(proc.pid, mask and allMask)
                    onDismiss()
                },
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun AffinityCheckRow(
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    onToggle: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onToggle() }
            .padding(vertical = 1.dp, horizontal = 2.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(2.dp))
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Small uppercase section label with a hairline rule filling the remaining row width. Mirrors the
 *  mockup's `.seclabel::after`. [leading] slots an icon before the text (used by the CONTAINER header). */
@Composable
private fun TmSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
    ) {
        leading?.invoke()
        Text(
            text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.7.sp,
        )
        Spacer(Modifier.width(8.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        )
    }
}

/** Dense 4-column stat-tile grid fed by the polled HudMetrics snapshot + FPS (mockup's PERFORMANCE
 *  block). CPU/GPU are accent-tinted, FPS reads "ok" green, battery reads "warn" amber; every
 *  nullable/Mali metric still renders gracefully as "—". */
@Composable
private fun TmStatGrid(h: XServerDialogState.TmHeaderStats?) {
    if (h == null) return
    val accent = MaterialTheme.colorScheme.primary
    val onSurf = MaterialTheme.colorScheme.onSurface
    val dark = isSystemInDarkTheme()
    val ok = if (dark) Color(0xFF6FDF9A) else Color(0xFF1F9D57)     // FPS — reads on both themes
    val warn = if (dark) Color(0xFFE2B06F) else Color(0xFFB5761A)   // battery

    // Leading numeric part of a "5.3GiB" style string, and a compact "GiB"->"G" unit form.
    fun numOf(s: String) = s.takeWhile { it.isDigit() || it == '.' }.ifEmpty { s }
    fun compact(s: String) = s.replace("iB", "")

    val tiles = buildList {
        add(TmTile("CPU", h.cpuPct?.let { "$it%" } ?: "—", h.cpuTempC?.let { " $it°" } ?: "", accent))
        add(TmTile("GPU", h.gpuPct?.let { "$it%" } ?: "—", h.gpuTempC?.let { " $it°" } ?: "", accent))
        add(TmTile("GPU CLK", h.gpuClockMhz?.let { "$it" } ?: "—", if (h.gpuClockMhz != null) "MHz" else "", onSurf))
        add(TmTile("FPS", "${h.fps}", if (h.fpsMin > 0) " ·${h.fpsMin}min" else "", ok))
        add(TmTile("RAM", numOf(h.ramUsed), "/" + compact(h.ramTotal), onSurf))
        if (h.swap != null) {
            add(TmTile("SWAP", numOf(h.swap.substringBefore("/")), "/" + compact(h.swap.substringAfter("/")), onSurf))
        } else {
            add(TmTile("SWAP", "—", "", onSurf))
        }
        add(TmTile("BAT", h.batteryPct?.let { "$it%" } ?: "—",
            " " + String.format("%.1f", h.batteryWatts) + "W" + (if (h.charging) " ⚡" else ""), warn))
        add(TmTile("BAT °C", h.batteryTempC?.let { "$it°" } ?: "—", "", onSurf))
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        TmSectionLabel("PERFORMANCE")
        Spacer(Modifier.height(4.dp))
        tiles.chunked(4).forEach { rowTiles ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.5.dp)) {
                rowTiles.forEachIndexed { i, t ->
                    StatTile(t, Modifier.weight(1f))
                    if (i < rowTiles.size - 1) Spacer(Modifier.width(5.dp))
                }
                // Pad a short final row so tiles keep the 4-column width.
                repeat(4 - rowTiles.size) {
                    Spacer(Modifier.width(5.dp))
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatTile(tile: TmTile, modifier: Modifier = Modifier) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 7.dp, vertical = 6.dp),
    ) {
        Text(
            tile.caption,
            color = muted,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = tile.tint)) {
                    append(tile.big)
                }
                if (tile.small.isNotEmpty()) {
                    withStyle(SpanStyle(fontSize = 9.sp, fontWeight = FontWeight.Medium, color = muted)) {
                        append(tile.small)
                    }
                }
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 1.dp),
        )
    }
}

/** Horizontal, scrollable per-core current-clock strip. Values are shown in FULL MHz (the raw
 *  perCoreMhz integer, NOT abbreviated to GHz) with a tiny "MHz" unit. */
@Composable
private fun TmCoreStrip(h: XServerDialogState.TmHeaderStats?) {
    val cores = h?.perCoreMhz ?: return
    if (cores.isEmpty()) return
    val accent = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        CorePanel(count = cores.size) { i ->
            val mhz = cores[i]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
            ) {
                Text("C$i", color = muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accent)) {
                            append(if (mhz > 0) "$mhz" else "—")
                        }
                        if (mhz > 0) {
                            withStyle(SpanStyle(fontSize = 8.sp, fontWeight = FontWeight.Medium, color = muted)) {
                                append(" MHz")
                            }
                        }
                    },
                    maxLines = 1,
                )
            }
        }
    }
}

/** The cores as ONE shaded panel — the translucent accent wash used by the Vulkan/DXVK game-card
 *  component pills — with hairline dividers BETWEEN cells only (no per-cell borders, no outer-edge
 *  lines). 4-per-row; scales to any count. [cell] renders one core's content by 0-based index.
 *  Vertical hairlines use a Box rather than VerticalDivider (not on the classpath). */
@Composable
private fun CorePanel(
    count: Int,
    cols: Int = 4,
    cell: @Composable (Int) -> Unit,
) {
    if (count <= 0) return
    val accent = MaterialTheme.colorScheme.primary
    val div = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    val rows = (count + cols - 1) / cols
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.38f), RoundedCornerShape(12.dp)),
    ) {
        for (r in 0 until rows) {
            if (r > 0) HorizontalDivider(thickness = 1.dp, color = div)
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                for (c in 0 until cols) {
                    val idx = r * cols + c
                    if (c > 0) {
                        if (idx < count) {
                            Box(Modifier.width(1.dp).fillMaxHeight().background(div))
                        } else {
                            Spacer(Modifier.width(1.dp))
                        }
                    }
                    if (idx < count) {
                        Box(Modifier.weight(1f)) { cell(idx) }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** Collapsible panel showing the running container's config (Wine/DX/renderer/driver/res/device).
 *  DX wrapper + Renderer render in the accent colour; the raw resolved ids are prettified. */
@Composable
private fun TmContainerPanel(info: XServerDialogState.TmContainerInfo?) {
    if (info == null) return
    val accent = MaterialTheme.colorScheme.primary
    var expanded by remember { mutableStateOf(true) }
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        TmSectionLabel(
            "CONTAINER",
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { expanded = !expanded },
            leading = {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(3.dp))
            },
        )
        if (expanded) {
            Spacer(Modifier.height(4.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                ContainerInfoRow("Wine", info.wine)
                ContainerInfoRow("DX wrapper", DrawerPrettyDxWrapper(info.dxWrapper), accent)
                ContainerInfoRow("Renderer", DrawerPrettyRenderer(info.renderer), accent)
                ContainerInfoRow("Graphics driver", info.graphicsDriver)
                ContainerInfoRow("Resolution", info.resolution)
                ContainerInfoRow("Device", DrawerTidyDevice(info.device))
            }
        }
    }
}

@Composable
private fun ContainerInfoRow(label: String, value: String, valueColor: Color? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.weight(0.42f))
        Text(
            value,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.58f),
        )
    }
}
package com.winlator.star.ui.screens.drawer

import com.winlator.star.R
import com.winlator.star.ui.TabType

/**
 * Workstream B - B1.3: per-tab rail metadata (pure UI, no business logic).
 *
 * FAITHFUL ADAPTATION: the plan's original six tabs (Graphics / Performance / Audio /
 * Input / Overlay / About) do not exist in this codebase. The real drawer drives EIGHT
 * tabs through the [TabType] enum (GRAPHICS, HUD, RESHADE, CONTROLS, AUDIO, ADVANCED,
 * TASK_MANAGER, TV). This sealed interface is the actual-tab metadata used by
 * [DrawerRail]: each entry maps a rail button to its [TabType] and optional icon
 * drawable. HUD and TV render as text pills ("FPS" / "TV") instead of icons, mirroring
 * the original FpsTabButton / TvTabButton composables.
 */
sealed interface DrawerTab {
    /** The real drawer tab this rail button drives. */
    val tabType: TabType

    /**
     * Icon drawable for the rail pill, or null for tabs that render as a text pill
     * (HUD = "FPS", TV = "TV").
     */
    val iconRes: Int?

    data object Graphics : DrawerTab {
        override val tabType = TabType.GRAPHICS
        override val iconRes = R.drawable.icon_display
    }

    data object Hud : DrawerTab {
        override val tabType = TabType.HUD
        override val iconRes = null
    }

    data object Reshade : DrawerTab {
        override val tabType = TabType.RESHADE
        override val iconRes = R.drawable.icon_screen_effect
    }

    data object Controls : DrawerTab {
        override val tabType = TabType.CONTROLS
        override val iconRes = R.drawable.icon_input_controls
    }

    data object Audio : DrawerTab {
        override val tabType = TabType.AUDIO
        override val iconRes = R.drawable.icon_audio
    }

    data object Advanced : DrawerTab {
        override val tabType = TabType.ADVANCED
        override val iconRes = R.drawable.icon_debug
    }

    data object Tv : DrawerTab {
        override val tabType = TabType.TV
        override val iconRes = null
    }

    data object TaskManager : DrawerTab {
        override val tabType = TabType.TASK_MANAGER
        override val iconRes = R.drawable.icon_task_manager
    }

    companion object {
        /**
         * Top-group rail order (step B1.1, faithful to the existing layout). The TV tab is
         * excluded here because it is conditionally rendered by [DrawerRail] when
         * FeatureFlags.TV_OUTPUT_ENABLED is on AND a TV/caster is connected;
         * TaskManager lives in the rail's bottom action group.
         */
        val topTabs: List<DrawerTab> = listOf(
            Graphics, Hud, Reshade, Controls, Audio, Advanced
        )
    }
}
package com.winlator.star.perf

import com.winlator.star.perf.TempWatchdog.ThresholdMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM coverage for the thermal-watchdog ceiling resolution (Phase 4: thermal trip test). Exercises
 * the pure [TempWatchdog.resolvedCeilingFor] seam: trip-point anchoring, per-mode margins, the
 * no-device-trips fallback, and the MANUAL clamp — the values that decide when the failsafe
 * force-reverts a hot device.
 */
class TempWatchdogCeilingTest {

    // ── CONSERVATIVE anchors to the FIRST (soft) throttle trip ────────────────────────────────────

    @Test fun conservative_usesFirstTrip() {
        assertEquals(95, TempWatchdog.resolvedCeilingFor(ThresholdMode.CONSERVATIVE, 95, 115, 90))
    }

    @Test fun conservative_fallsBackWhenNoFirstTrip_evenIfTopExists() {
        // No soft trip exposed -> fixed fallback, NOT a derivation from the hard trip.
        assertEquals(TempWatchdog.FALLBACK_CEILING_C,
            TempWatchdog.resolvedCeilingFor(ThresholdMode.CONSERVATIVE, null, 115, 90))
    }

    // ── BALANCED / AGGRESSIVE anchor to the TOP (hard) trip minus their margins ───────────────────

    @Test fun balanced_isTopTripMinus10() {
        assertEquals(100, TempWatchdog.resolvedCeilingFor(ThresholdMode.BALANCED, 95, 110, 90))
        assertEquals(78, TempWatchdog.resolvedCeilingFor(ThresholdMode.BALANCED, 70, 88, 90))
    }

    @Test fun aggressive_isTopTripMinus3() {
        assertEquals(107, TempWatchdog.resolvedCeilingFor(ThresholdMode.AGGRESSIVE, 95, 110, 90))
        assertEquals(83, TempWatchdog.resolvedCeilingFor(ThresholdMode.AGGRESSIVE, 70, 86, 90))
    }

    @Test fun balancedAndAggressive_fallBackWhenNoTopTrip() {
        assertEquals(TempWatchdog.FALLBACK_CEILING_C,
            TempWatchdog.resolvedCeilingFor(ThresholdMode.BALANCED, 90, null, 90))
        assertEquals(TempWatchdog.FALLBACK_CEILING_C,
            TempWatchdog.resolvedCeilingFor(ThresholdMode.AGGRESSIVE, 90, null, 90))
    }

    @Test fun fallbackUsed_whenDeviceExposesNoTripsAtAll() {
        for (mode in listOf(ThresholdMode.CONSERVATIVE, ThresholdMode.BALANCED, ThresholdMode.AGGRESSIVE)) {
            assertEquals(TempWatchdog.FALLBACK_CEILING_C,
                TempWatchdog.resolvedCeilingFor(mode, null, null, 90))
        }
    }

    // ── Ordering invariant: more aggressive mode => hotter ceiling on a trip-anchored device ──────

    @Test fun aggressive_hotterThanBalanced_hotterThanConservative() {
        val first = 95; val top = 115
        val c = TempWatchdog.resolvedCeilingFor(ThresholdMode.CONSERVATIVE, first, top, 90)
        val b = TempWatchdog.resolvedCeilingFor(ThresholdMode.BALANCED, first, top, 90)
        val a = TempWatchdog.resolvedCeilingFor(ThresholdMode.AGGRESSIVE, first, top, 90)
        assertTrue("conservative $c must be <= balanced $b", c <= b)
        assertTrue("balanced $b must be < aggressive $a", b < a)
        // And every ceiling must stay BELOW the hard trip (never trip the device's own limit).
        assertTrue("aggressive $a must stay under top trip $top", a < top)
    }

    // ── MANUAL clamps to [MANUAL_MIN, min(top, MANUAL_MAX)] ───────────────────────────────────────

    @Test fun manual_clampsBelowMin() {
        assertEquals(TempWatchdog.MANUAL_MIN_C,
            TempWatchdog.resolvedCeilingFor(ThresholdMode.MANUAL, 95, 110, 40))
    }

    @Test fun manual_capsAtTopTrip() {
        assertEquals(110, TempWatchdog.resolvedCeilingFor(ThresholdMode.MANUAL, 95, 110, 130))
    }

    @Test fun manual_capsAtManualMax_whenNoDeviceTrips() {
        assertEquals(TempWatchdog.MANUAL_MAX_C,
            TempWatchdog.resolvedCeilingFor(ThresholdMode.MANUAL, null, null, 200))
    }

    @Test fun manual_passesThroughInRange() {
        assertEquals(90, TempWatchdog.resolvedCeilingFor(ThresholdMode.MANUAL, 95, 110, 90))
    }

    @Test fun manual_upperBoundNeverBelowMin_onOddDeviceWithTinyTopTrip() {
        // A device reporting top=55 must not produce min>max: the clamp floor wins.
        assertEquals(TempWatchdog.MANUAL_MIN_C,
            TempWatchdog.resolvedCeilingFor(ThresholdMode.MANUAL, 50, 55, 90))
    }
}

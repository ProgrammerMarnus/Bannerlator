package com.winlator.star.container;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable RUNTIME-only state for a container. Holds nothing persisted ({@link ContainerConfig}
 * owns the disk shape) and nothing about settings — just the live session facts a hot container
 * needs while Wine is booting or running. The legacy {@code Container.java} keeps its own mutable
 * fields today; this type is the new home for that runtime half once the facade is rewired (a
 * build-toolchain follow-up).
 *
 * <p>Pure JVM so it is trivially unit-testable. Runtime state, unlike config, is explicitly
 * mutable and intentionally has NO equality contract (identity semantics — one field changing
 * should not make it "unequal").
 */
public final class ContainerState {
    /** Wine process id while running, or 0 when stopped. */
    private int runningPid = 0;
    /** Opaque session id issued on each launch; unique while the container runs. */
    private String sessionId = "";
    /** Epoch millis of the most recent launch; 0 if never launched this process lifetime. */
    private long lastLaunched = 0L;
    /** Active window titles/ids in the running session; empty when not running. */
    private final List<String> activeWindows = new ArrayList<>();
    /** Last resolution string the session reported, e.g. "1280x720"; "" when unknown. */
    private String currentResolution = "";

    public int getRunningPid() {
        return runningPid;
    }

    public void setRunningPid(int runningPid) {
        this.runningPid = runningPid;
    }

    public boolean isRunning() {
        return runningPid != 0;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId == null ? "" : sessionId;
    }

    public long getLastLaunched() {
        return lastLaunched;
    }

    public void setLastLaunched(long lastLaunched) {
        this.lastLaunched = lastLaunched;
    }

    public List<String> getActiveWindows() {
        return activeWindows;
    }

    public void clearActiveWindows() {
        activeWindows.clear();
    }

    public boolean addActiveWindow(String windowId) {
        return activeWindows.add(windowId);
    }

    public String getCurrentResolution() {
        return currentResolution;
    }

    public void setCurrentResolution(String currentResolution) {
        this.currentResolution = currentResolution == null ? "" : currentResolution;
    }

    /** Resets all runtime fields back to the just-instantiated state (called on stop / teardown). */
    public void reset() {
        runningPid = 0;
        sessionId = "";
        lastLaunched = 0L;
        activeWindows.clear();
        currentResolution = "";
    }

    @Override
    public String toString() {
        return "ContainerState{runningPid=" + runningPid + ", sessionId='" + sessionId + "'"
                + ", lastLaunched=" + lastLaunched + ", activeWindows=" + activeWindows.size()
                + ", resolution='" + currentResolution + "'}";
    }
}
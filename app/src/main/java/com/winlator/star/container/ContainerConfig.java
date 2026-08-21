package com.winlator.star.container;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Immutable snapshot of every persisted container setting, matching the JSON keys the legacy
 * {@code Container#getData()}/{@code loadData()} already read/write so an existing
 * {@code .container} file stays byte-compatible. This is the config half of the phase-1 container
 * refactor - the shape the new {@link ContainerRepository} boundary works with.
 *
 * <p>Pure JVM (no android types) so the unit tests under {@code app/src/test} can exercise the
 * round-trip and migration logic without a device copy of {@code Container}. This is ADDITIVE:
 * the shipped {@code Container.java} facade is not rewired to it on this change.
 */
public final class ContainerConfig {
    /** Enum mirror of {@code Container.XrControllerMapping}. */
    public enum XrControllerMapping {
        BUTTON_A, BUTTON_B, BUTTON_X, BUTTON_Y, BUTTON_GRIP, BUTTON_TRIGGER,
        THUMBSTICK_UP, THUMBSTICK_DOWN, THUMBSTICK_LEFT, THUMBSTICK_RIGHT
    }

    public static final String DEFAULT_ENV_VARS =
            "WRAPPER_MAX_IMAGE_COUNT=0 ZINK_DESCRIPTORS=lazy ZINK_DEBUG=compact MESA_SHADER_CACHE_DISABLE=false " +
            "MESA_SHADER_CACHE_MAX_SIZE=512MB mesa_glthread=true WINEESYNC=1 TU_DEBUG=noconform,sysmem " +
            "DXVK_HUD=devinfo,fps,frametimes,gpuload,version,api";
    public static final String DEFAULT_SCREEN_SIZE = "1280x720";
    public static final String DEFAULT_GRAPHICS_DRIVER = "wrapper";
    public static final String DEFAULT_AUDIO_DRIVER = "pulseaudio";
    public static final String DEFAULT_DXWRAPPER = "dxvk+vkd3d";
    public static final String DEFAULT_GRAPHICSDRIVERCONFIG =
            "vulkanVersion=1.4;version=;maxDeviceMemory=0;presentMode=mailbox;syncFrame=0;gpuName=Device";
    public static final String DEFAULT_FPS_COUNTER_CONFIG =
            "hudStyle=fusion,hudEnabled=1,hudMode=horizontal,showFPS=1,hudScale=100";
    public static final String DEFAULT_WINCOMPONENTS =
            "direct3d=1,directsound=0,directmusic=0,directshow=0,directplay=0,xaudio=0,vcrun2010=1";
    public static final int FULLSCREEN_OFF = 0;
    public static final int FULLSCREEN_STRETCH = 2;
    public static final byte STARTUP_SELECTION_ESSENTIAL = 1;
    public static final byte STARTUP_SELECTION_CUSTOM = 3;
    public static final String DEFAULT_DESKTOP_THEME = "LIGHT,IMAGE,#0277bd";
    public static final String DEFAULT_RENDERER = "vulkan";
    public static final byte DEFAULT_INPUT_TYPE = 0x04;

    // ── persisted scalar fields ──────────────────────────────────────────────────────────────
    public final int id;
    public final String name;
    public final String screenSize;
    public final String envVars;
    public final String cpuList;
    public final String cpuListWoW64;
    public final String graphicsDriver;
    public final String graphicsDriverConfig;
    public final String emulator;
    public final String dxwrapper;
    public final String dxwrapperConfig;
    public final String audioDriver;
    public final String wincomponents;
    public final String drives;
    public final boolean showFPS;
    public final String fpsCounterConfig;
    public final int fullscreenMode;
    public final int inputType;
    public final byte startupSelection;
    public final String startupServices;
    public final String box64Version;
    public final String fexcorePreset;
    public final String fexcoreVersion;
    public final String box64Preset;
    public final String desktopTheme;
    public final String midiSoundFont;
    public final String lc_all;
    public final int primaryController;
    public final String controllerMapping;
    public final boolean exclusiveXInput;
    public final String renderer;
    public final boolean rendererNative;
    public final String rendererPresentMode;
    public final String rendererDriverId;
    public final int rendererFilterMode;
    public final boolean rendererSfCompatMode;
    public final String wineVersion;
    /** Immutable snapshot of any dynamic extras; never null. */
    public final JSONObject extras;

    private ContainerConfig(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.screenSize = b.screenSize;
        this.envVars = b.envVars;
        this.cpuList = b.cpuList;
        this.cpuListWoW64 = b.cpuListWoW64;
        this.graphicsDriver = b.graphicsDriver;
        this.graphicsDriverConfig = b.graphicsDriverConfig;
        this.emulator = b.emulator;
        this.dxwrapper = b.dxwrapper;
        this.dxwrapperConfig = b.dxwrapperConfig;
        this.audioDriver = b.audioDriver;
        this.wincomponents = b.wincomponents;
        this.drives = b.drives;
        this.showFPS = b.showFPS;
        this.fpsCounterConfig = b.fpsCounterConfig;
        this.fullscreenMode = b.fullscreenMode;
        this.inputType = b.inputType;
        this.startupSelection = b.startupSelection;
        this.startupServices = b.startupServices;
        this.box64Version = b.box64Version;
        this.fexcorePreset = b.fexcorePreset;
        this.fexcoreVersion = b.fexcoreVersion;
        this.box64Preset = b.box64Preset;
        this.desktopTheme = b.desktopTheme;
        this.midiSoundFont = b.midiSoundFont;
        this.lc_all = b.lc_all;
        this.primaryController = b.primaryController;
        this.controllerMapping = b.controllerMapping;
        this.exclusiveXInput = b.exclusiveXInput;
        this.renderer = b.renderer;
        this.rendererNative = b.rendererNative;
        this.rendererPresentMode = b.rendererPresentMode;
        this.rendererDriverId = b.rendererDriverId;
        this.rendererFilterMode = b.rendererFilterMode;
        this.rendererSfCompatMode = b.rendererSfCompatMode;
        this.wineVersion = b.wineVersion;
        this.extras = b.extras == null ? new JSONObject() : b.extras;
    }

    public static Builder builder(int id) {
        return new Builder(id);
    }

    // ── Serialization (same keys Container#getData()/loadData() already use) ─────────────────
    public JSONObject toJson() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("id", id);
        data.put("name", name);
        data.put("screenSize", screenSize);
        data.put("envVars", envVars);
        data.put("cpuList", cpuList != null ? cpuList : "");
        data.put("cpuListWoW64", cpuListWoW64 != null ? cpuListWoW64 : "");
        data.put("graphicsDriver", graphicsDriver);
        data.put("graphicsDriverConfig", graphicsDriverConfig);
        data.put("emulator", emulator);
        data.put("dxwrapper", dxwrapper);
        if (dxwrapperConfig != null && !dxwrapperConfig.isEmpty()) data.put("dxwrapperConfig", dxwrapperConfig);
        data.put("audioDriver", audioDriver);
        data.put("wincomponents", wincomponents);
        data.put("drives", drives);
        data.put("showFPS", showFPS);
        data.put("fpsCounterConfig", fpsCounterConfig);
        data.put("fullscreenMode", fullscreenMode);
        data.put("inputType", inputType);
        data.put("startupSelection", startupSelection);
        data.put("startupServices", startupServices != null ? startupServices : "");
        data.put("box64Version", box64Version);
        data.put("fexcorePreset", fexcorePreset);
        data.put("fexcoreVersion", fexcoreVersion);
        data.put("box64Preset", box64Preset);
        data.put("desktopTheme", desktopTheme);
        if (extras != null && extras.length() > 0) data.put("extraData", extras);
        data.put("midiSoundFont", midiSoundFont != null ? midiSoundFont : "");
        data.put("lc_all", lc_all != null ? lc_all : "");
        data.put("primaryController", primaryController);
        data.put("controllerMapping", controllerMapping);
        data.put("exclusiveXInput", exclusiveXInput);
        data.put("renderer", renderer);
        data.put("rendererNative", rendererNative);
        data.put("rendererPresentMode", rendererPresentMode);
        if (rendererDriverId != null && !rendererDriverId.isEmpty()) data.put("rendererDriverId", rendererDriverId);
        if (rendererFilterMode != 0) data.put("rendererFilterMode", rendererFilterMode);
        if (!rendererSfCompatMode) data.put("rendererSfCompatMode", false);
        data.put("wineVersion", wineVersion);
        return data;
    }

    public String toJsonString() {
        try {
            return toJson().toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    public static ContainerConfig fromJson(JSONObject data) throws JSONException {
        if (data == null) data = new JSONObject();
        int id = data.optInt("id", 0);
        String name = data.optString("name", "Container-" + id);

        JSONObject extras = new JSONObject();
        if (data.has("extraData")) extras = data.getJSONObject("extraData");

        String controllerMapping = new String(new char[XrControllerMapping.values().length]);
        if (data.has("controllerMapping")) controllerMapping = data.getString("controllerMapping");

        return new Builder(id)
                .name(name)
                .screenSize(data.optString("screenSize", DEFAULT_SCREEN_SIZE))
                .envVars(data.optString("envVars", DEFAULT_ENV_VARS))
                .cpuList(data.optString("cpuList", ""))
                .cpuListWoW64(data.optString("cpuListWoW64", ""))
                .graphicsDriver(data.optString("graphicsDriver", DEFAULT_GRAPHICS_DRIVER))
                .graphicsDriverConfig(data.optString("graphicsDriverConfig", DEFAULT_GRAPHICSDRIVERCONFIG))
                .emulator(data.optString("emulator", null))
                .dxwrapper(data.optString("dxwrapper", DEFAULT_DXWRAPPER))
                .dxwrapperConfig(data.optString("dxwrapperConfig", ""))
                .audioDriver(data.optString("audioDriver", DEFAULT_AUDIO_DRIVER))
                .wincomponents(data.optString("wincomponents", DEFAULT_WINCOMPONENTS))
                .drives(data.optString("drives", ""))
                .showFPS(data.optBoolean("showFPS", false))
                .fpsCounterConfig(data.optString("fpsCounterConfig", DEFAULT_FPS_COUNTER_CONFIG))
                .fullscreenMode(data.optInt("fullscreenMode", FULLSCREEN_OFF))
                .inputType(data.optInt("inputType", DEFAULT_INPUT_TYPE))
                .startupSelection((byte) data.optInt("startupSelection", STARTUP_SELECTION_ESSENTIAL))
                .startupServices(data.optString("startupServices", ""))
                .box64Version(data.optString("box64Version", null))
                .fexcorePreset(data.optString("fexcorePreset", "INTERMEDIATE"))
                .fexcoreVersion(data.optString("fexcoreVersion", null))
                .box64Preset(data.optString("box64Preset", "COMPATIBILITY"))
                .desktopTheme(data.optString("desktopTheme", DEFAULT_DESKTOP_THEME))
                .midiSoundFont(data.optString("midiSoundFont", ""))
                .lc_all(data.optString("lc_all", ""))
                .primaryController(data.optInt("primaryController", 1))
                .controllerMapping(controllerMapping)
                .exclusiveXInput(data.optBoolean("exclusiveXInput", true))
                .renderer(data.optString("renderer", DEFAULT_RENDERER))
                .rendererNative(data.optBoolean("rendererNative", false))
                .rendererPresentMode(data.optString("rendererPresentMode", "fifo"))
                .rendererDriverId(data.optString("rendererDriverId", "system"))
                .rendererFilterMode(data.optInt("rendererFilterMode", 0))
                .rendererSfCompatMode(data.optBoolean("rendererSfCompatMode", true))
                .wineVersion(data.optString("wineVersion", "proton-9.0-x86_64"))
                .extras(extras)
                .build();
    }

    public static ContainerConfig fromJson(String json) throws JSONException {
        return fromJson(json == null || json.isEmpty() ? new JSONObject() : new JSONObject(json));
    }

    public String getExtra(String name) {
        return extras != null && extras.has(name) ? extras.optString(name) : null;
    }

    public String getExtra(String name, String fallback) {
        String v = getExtra(name);
        return v != null ? v : fallback;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContainerConfig)) return false;
        ContainerConfig that = (ContainerConfig) o;
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(screenSize, that.screenSize) &&
                Objects.equals(envVars, that.envVars) &&
                Objects.equals(graphicsDriver, that.graphicsDriver) &&
                Objects.equals(renderer, that.renderer) &&
                fullscreenMode == that.fullscreenMode &&
                rendererNative == that.rendererNative &&
                rendererSfCompatMode == that.rendererSfCompatMode &&
                Objects.equals(extras.toString(), that.extras.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, screenSize, envVars, graphicsDriver, renderer,
                fullscreenMode, rendererNative, rendererSfCompatMode, extras.toString());
    }

    @Override
    public String toString() {
        return "ContainerConfig{id=" + id + ", name='" + name + "', screenSize='" + screenSize + "'}";
    }
    public static final class Builder {
        private final int id;
        private String name;
        private String screenSize = DEFAULT_SCREEN_SIZE;
        private String envVars = DEFAULT_ENV_VARS;
        private String cpuList = "";
        private String cpuListWoW64 = "";
        private String graphicsDriver = DEFAULT_GRAPHICS_DRIVER;
        private String graphicsDriverConfig = DEFAULT_GRAPHICSDRIVERCONFIG;
        private String emulator;
        private String dxwrapper = DEFAULT_DXWRAPPER;
        private String dxwrapperConfig = "";
        private String audioDriver = DEFAULT_AUDIO_DRIVER;
        private String wincomponents = DEFAULT_WINCOMPONENTS;
        private String drives = "";
        private boolean showFPS;
        private String fpsCounterConfig = DEFAULT_FPS_COUNTER_CONFIG;
        private int fullscreenMode = FULLSCREEN_OFF;
        private int inputType = DEFAULT_INPUT_TYPE;
        private byte startupSelection = STARTUP_SELECTION_ESSENTIAL;
        private String startupServices = "";
        private String box64Version;
        private String fexcorePreset = "INTERMEDIATE";
        private String fexcoreVersion;
        private String box64Preset = "COMPATIBILITY";
        private String desktopTheme = DEFAULT_DESKTOP_THEME;
        private String midiSoundFont = "";
        private String lc_all = "";
        private int primaryController = 1;
        private String controllerMapping = new String(new char[XrControllerMapping.values().length]);
        private boolean exclusiveXInput = true;
        private String renderer = DEFAULT_RENDERER;
        private boolean rendererNative;
        private String rendererPresentMode = "fifo";
        private String rendererDriverId = "system";
        private int rendererFilterMode;
        private boolean rendererSfCompatMode = true;
        private String wineVersion = "proton-9.0-x86_64";
        private JSONObject extras = new JSONObject();

        public Builder(int id) {
            this.id = id;
            this.name = "Container-" + id;
        }

        public Builder name(String v) { this.name = v; return this; }
        public Builder screenSize(String v) { this.screenSize = v; return this; }
        public Builder envVars(String v) { this.envVars = v; return this; }
        public Builder cpuList(String v) { this.cpuList = v; return this; }
        public Builder cpuListWoW64(String v) { this.cpuListWoW64 = v; return this; }
        public Builder graphicsDriver(String v) { this.graphicsDriver = v; return this; }
        public Builder graphicsDriverConfig(String v) { this.graphicsDriverConfig = v; return this; }
        public Builder emulator(String v) { this.emulator = v; return this; }
        public Builder dxwrapper(String v) { this.dxwrapper = v; return this; }
        public Builder dxwrapperConfig(String v) { this.dxwrapperConfig = v; return this; }
        public Builder audioDriver(String v) { this.audioDriver = v; return this; }
        public Builder wincomponents(String v) { this.wincomponents = v; return this; }
        public Builder drives(String v) { this.drives = v; return this; }
        public Builder showFPS(boolean v) { this.showFPS = v; return this; }
        public Builder fpsCounterConfig(String v) { this.fpsCounterConfig = v; return this; }
        public Builder fullscreenMode(int v) { this.fullscreenMode = v; return this; }
        public Builder inputType(int v) { this.inputType = v; return this; }
        public Builder startupSelection(byte v) { this.startupSelection = v; return this; }
        public Builder startupServices(String v) { this.startupServices = v; return this; }
        public Builder box64Version(String v) { this.box64Version = v; return this; }
        public Builder fexcorePreset(String v) { this.fexcorePreset = v; return this; }
        public Builder fexcoreVersion(String v) { this.fexcoreVersion = v; return this; }
        public Builder box64Preset(String v) { this.box64Preset = v; return this; }
        public Builder desktopTheme(String v) { this.desktopTheme = v; return this; }
        public Builder midiSoundFont(String v) { this.midiSoundFont = v; return this; }
        public Builder lc_all(String v) { this.lc_all = v; return this; }
        public Builder primaryController(int v) { this.primaryController = v; return this; }
        public Builder controllerMapping(String v) { this.controllerMapping = v; return this; }
        public Builder exclusiveXInput(boolean v) { this.exclusiveXInput = v; return this; }
        public Builder renderer(String v) { this.renderer = v; return this; }
        public Builder rendererNative(boolean v) { this.rendererNative = v; return this; }
        public Builder rendererPresentMode(String v) { this.rendererPresentMode = v; return this; }
        public Builder rendererDriverId(String v) { this.rendererDriverId = v; return this; }
        public Builder rendererFilterMode(int v) { this.rendererFilterMode = v; return this; }
        public Builder rendererSfCompatMode(boolean v) { this.rendererSfCompatMode = v; return this; }
        public Builder wineVersion(String v) { this.wineVersion = v; return this; }
        public Builder extras(JSONObject v) { this.extras = v; return this; }

        public ContainerConfig build() {
            return new ContainerConfig(this);
        }
    }
}
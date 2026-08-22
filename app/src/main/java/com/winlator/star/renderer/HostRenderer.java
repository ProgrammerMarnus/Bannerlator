package com.winlator.star.renderer;

import com.winlator.star.widget.XServerView;

import java.util.function.IntConsumer;

public interface HostRenderer {
    XServerView getXServerView();
    void setRenderingEnabled(boolean enabled);
    void requestRender();
    void forceCleanup();
    void setCursorVisible(boolean visible);
    boolean isCursorVisible();
    void setUnviewableWMClasses(String wmClasses);
    void setFilterMode(int mode);
    void setMagnifierZoom(float zoom);
    float getMagnifierZoom();
    void toggleFullscreen();
    boolean isFullscreen();
    // Fullscreen aspect-ratio mode (issue #71): Container.FULLSCREEN_OFF/FIT/STRETCH/...
    // isFullscreen() stays == (mode != OFF) so existing upscaler/magnifier gates behave as before.
    void setFullscreenMode(int mode);
    int getFullscreenMode();
    void setScreenOffsetYRelativeToCursor(boolean b);
    boolean isScreenOffsetYRelativeToCursor();
    void setFpsWindowId(int id);
    void setFrameRating(Object fr);
    int getFpsLimit();
    void setFpsLimit(int limit);
    int getSurfaceWidth();
    int getSurfaceHeight();
    // Performance overlay tick: called from the render/present thread when a new window frame
    // is presented, so the FPS HUD can update. Set by the activity or overlay layer.
    void setHudFrameTick(IntConsumer tick);
    // Direct-scanout (native mode) rendering: when enabled, the renderer hands the game surface
    // directly to the compositor and minimizes host-side composition. Mirrors VulkanRenderer/GLRenderer.
    void setNativeMode(boolean enabled);
    boolean isNativeMode();
    // Window-texture filter used by the magnifier and cursor overlays (GLRenderer only;
    // others are no-ops for consistency).
    void setWindowTexFilter(int filter);
}

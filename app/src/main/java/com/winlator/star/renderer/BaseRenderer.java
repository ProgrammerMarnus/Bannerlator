package com.winlator.star.renderer;

import com.winlator.star.container.Container;
import com.winlator.star.xserver.XServer;
import com.winlator.star.widget.XServerView;

/**
 * Abstract base class for the {@link HostRenderer} implementations (GLRenderer, VulkanRenderer,
 * ASurfaceRenderer). Owns the state + trivial accessors that were previously duplicated in each
 * renderer: surface dimensions, fullscreen mode, magnifier/offset toggles, cursor visibility,
 * native (direct-scanout) mode, fps limiting/window, unviewable WM classes, and the HUD tick.
 *
 * <p>Subclasses keep their backend lifecycle (GL draw loop, Vulkan present thread, ASurface
 * transactions) and MUST override setters whose original versions had side effects — call
 * {@code super.setX(...)} first to update the shared field, then perform the backend action
 * (requestRender / native push / transform update). Fields are {@code volatile} because they are
 * written from the UI/service threads and read from render/present/epoll threads.
 */
public abstract class BaseRenderer implements HostRenderer {
    protected final XServerView xServerView;
    protected final XServer xServer;

    public final ViewTransformation viewTransformation = new ViewTransformation();

    protected volatile int surfaceWidth;
    protected volatile int surfaceHeight;

    // Fullscreen aspect-ratio mode (#71). STRETCH fills the surface (distorts); OFF/FIT letterbox.
    protected volatile int fullscreenMode = Container.FULLSCREEN_OFF;
    protected volatile boolean screenOffsetYRelativeToCursor = false;
    protected volatile float magnifierZoom = 1.0f;
    protected volatile boolean cursorVisible = true;
    protected volatile boolean nativeMode = false;
    protected volatile int fpsLimit = 0;
    protected volatile int fpsWindowId = -1;
    protected String[] unviewableWMClasses = null;
    /** Invoked from render/present threads with the presented window id; drives the perf HUD. */
    protected volatile java.util.function.IntConsumer hudFrameTick = null;

    protected boolean isStretch() { return fullscreenMode == Container.FULLSCREEN_STRETCH; }

    protected BaseRenderer(XServerView xServerView, XServer xServer) {
        this.xServerView = xServerView;
        this.xServer = xServer;
    }

    @Override
    public XServerView getXServerView() { return xServerView; }

    @Override
    public int getSurfaceWidth() { return surfaceWidth; }

    @Override
    public int getSurfaceHeight() { return surfaceHeight; }

    @Override
    public void setCursorVisible(boolean visible) { cursorVisible = visible; }

    @Override
    public boolean isCursorVisible() { return cursorVisible; }

    @Override
    public void setScreenOffsetYRelativeToCursor(boolean b) { screenOffsetYRelativeToCursor = b; }

    @Override
    public boolean isScreenOffsetYRelativeToCursor() { return screenOffsetYRelativeToCursor; }

    @Override
    public void setMagnifierZoom(float zoom) { magnifierZoom = zoom; }

    @Override
    public float getMagnifierZoom() { return magnifierZoom; }

    @Override
    public void setUnviewableWMClasses(String classes) {
        this.unviewableWMClasses = classes != null ? classes.split(";") : null;
    }

    @Override
    public void setFpsWindowId(int id) { fpsWindowId = id; }

    /** Default no-op; backends that keep a typed HUD ref override this. */
    @Override
    public void setFrameRating(Object fr) {}

    @Override
    public int getFpsLimit() { return fpsLimit; }

    @Override
    public void setFpsLimit(int limit) { fpsLimit = limit; }

    @Override
    public void setHudFrameTick(java.util.function.IntConsumer tick) { hudFrameTick = tick; }

    /**
     * Native (direct-scanout) mode. Only GL/Vulkan implement the real scanout lifecycle; the base
     * records the flag so {@link #isNativeMode()} is truthful everywhere, and the setter remains a
     * safe no-op on backends without a scanout path (ASurfaceRenderer).
     */
    @Override
    public void setNativeMode(boolean enabled) { nativeMode = enabled; }

    @Override
    public boolean isNativeMode() { return nativeMode; }

    /** Window-texture filter hint (GL tex filter enum); no-op by default (only GL consumes it). */
    @Override
    public void setWindowTexFilter(int filter) {}
}

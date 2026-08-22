package com.winlator.star.renderer.effects;

/**
 * Backend-agnostic pipeline contract for composable screen effects (plan Workstream A, Phase 3
 * task 4). {@link com.winlator.star.renderer.EffectComposer} is the GL implementation; the Vulkan
 * renderer implements the same operations natively (its "Phase 2 composable screen effects"
 * parity block). Coding against this interface lets future pipelines slot in without the callers
 * (ScreenEffectDialog, XServerDisplayActivity) knowing the backend.
 */
public interface EffectPipeline {
    /** Appends an effect to the end of the chain. Implementations must tolerate duplicates. */
    void addEffect(Effect effect);

    /** Removes a previously added effect instance. No-op when absent. */
    void removeEffect(Effect effect);

    /** Returns the registered effect of the given type, or {@code null}. */
    <T extends Effect> T getEffect(Class<T> effectClass);

    /** True when at least one effect is active and the pipeline must run its passes. */
    boolean isActive();

    /** Executes the pipeline (bind framebuffers, run passes, present to the default target). */
    void render();
}

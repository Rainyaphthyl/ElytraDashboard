package io.github.rainyaphthyl.elytradashboard.display;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.profiler.Profiler;

import javax.annotation.Nonnull;

public interface FrameUpdater {
    /**
     * Displaying the dashboard data. Called every frame on {@link EntityRenderer#updateCameraAndRender(float, long)} after rendering In-game GUI and before rendering GUI Screen
     *
     * @param partialTicks Partial tick value within a tick
     * @param inGame       True if in-game, false if in the menu
     */
    void render(float partialTicks, boolean inGame);

    /**
     * Query dashboard data from the server. Called every tick before {@link FrameUpdater#render} before {@link EntityRenderer#updateRenderer()}
     *
     * @param inGame True if in-game, false if in the menu
     */
    void tick(boolean inGame);

    @Nonnull
    Profiler getProfiler();

    default void pushProfiler(String name) {
        getProfiler().startSection(name);
    }

    default void popProfiler() {
        getProfiler().endSection();
    }

    @SuppressWarnings("unused")
    default void nextProfiler(String name) {
        getProfiler().endStartSection(name);
    }
}

package io.github.rainyaphthyl.elytradashboard.display;

import com.mumfrey.liteloader.client.mixin.MixinMinecraft;
import io.github.rainyaphthyl.elytradashboard.LiteModElytraDashboard;
import net.minecraft.client.Minecraft;
import net.minecraft.profiler.Profiler;

import javax.annotation.Nonnull;

public interface FrameUpdater {
    /**
     * Displaying the dashboard data. Called every frame on {@link LiteModElytraDashboard#onTick}
     *
     * @param partialTicks Partial tick value within a tick
     * @param inGame       True if in-game, false if in the menu
     */
    void render(float partialTicks, boolean inGame);

    /**
     * Query dashboard data from the server. Called every tick before {@link FrameUpdater#render} in {@link LiteModElytraDashboard#onTick} at only new ticks.
     * <p>
     * This method is mixed into {@link Minecraft#runGameLoop} by {@link MixinMinecraft#onTick}
     *
     * @param minecraft Minecraft game instance
     * @param inGame    True if in-game, false if in the menu
     */
    @SuppressWarnings("JavadocReference")
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

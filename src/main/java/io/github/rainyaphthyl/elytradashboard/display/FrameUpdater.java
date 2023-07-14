package io.github.rainyaphthyl.elytradashboard.display;

import io.github.rainyaphthyl.elytradashboard.LiteModElytraDashboard;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public interface FrameUpdater {
    /**
     * Displaying the dashboard data. Called every frame on {@link LiteModElytraDashboard#onTick}
     *
     * @param minecraft    Minecraft game instance
     * @param partialTicks Partial tick value within a tick
     */
    void render(@Nonnull Minecraft minecraft, float partialTicks);

    /**
     * Query dashboard data from the server. Called every tick on {@link LiteModElytraDashboard#onTick(Minecraft, float, boolean, boolean)} at only new ticks
     *
     * @param minecraft Minecraft game instance
     */
    void tick(@Nonnull Minecraft minecraft);
}

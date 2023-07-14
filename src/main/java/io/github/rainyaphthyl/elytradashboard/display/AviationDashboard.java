package io.github.rainyaphthyl.elytradashboard.display;

import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public class AviationDashboard implements FrameUpdater {
    @Override
    public void render(@Nonnull Minecraft minecraft, float partialTicks) {
        System.out.println("Render " + System.currentTimeMillis());
    }

    @Override
    public void tick(@Nonnull Minecraft minecraft) {
        System.out.println("Tick " + System.currentTimeMillis());
    }
}

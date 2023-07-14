package io.github.rainyaphthyl.elytradashboard.display;

import net.minecraft.client.Minecraft;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RegFrameUpdaters {
    private static final Map<String, FrameUpdater> REGISTRY = new LinkedHashMap<>();

    public static void registerAll() {
        REGISTRY.put("aviationDashboard", new AviationDashboard());
    }

    public static void updateAllOnFrame(Minecraft minecraft, float partialTicks) {
        for (Map.Entry<String, FrameUpdater> entry : REGISTRY.entrySet()) {
            FrameUpdater frameUpdater = entry.getValue();
            frameUpdater.render(minecraft, partialTicks);
        }
    }

    public static void updateAllOnTick(Minecraft minecraft) {
        for (Map.Entry<String, FrameUpdater> entry : REGISTRY.entrySet()) {
            FrameUpdater frameUpdater = entry.getValue();
            frameUpdater.tick(minecraft);
        }
    }
}

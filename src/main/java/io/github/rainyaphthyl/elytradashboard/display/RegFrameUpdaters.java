package io.github.rainyaphthyl.elytradashboard.display;

import net.minecraft.client.Minecraft;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RegFrameUpdaters {
    private static final Map<String, FrameUpdater> REGISTRY = new LinkedHashMap<>();

    public static void registerAll(Minecraft minecraft) {
        REGISTRY.put("the_dashboard", new ElytraReporter(minecraft));
    }

    public static void updateAllOnFrame(float partialTicks, boolean inGame) {
        for (Map.Entry<String, FrameUpdater> entry : REGISTRY.entrySet()) {
            FrameUpdater frameUpdater = entry.getValue();
            frameUpdater.pushProfiler(entry.getKey());
            frameUpdater.render(partialTicks, inGame);
            frameUpdater.popProfiler();
        }
    }

    public static void updateAllOnTick(boolean inGame) {
        for (Map.Entry<String, FrameUpdater> entry : REGISTRY.entrySet()) {
            FrameUpdater frameUpdater = entry.getValue();
            frameUpdater.pushProfiler(entry.getKey());
            frameUpdater.tick(inGame);
            frameUpdater.popProfiler();
        }
    }
}

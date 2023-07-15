package io.github.rainyaphthyl.elytradashboard.display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.profiler.Profiler;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Objects;
import java.util.Random;

public class AviationDashboard implements FrameUpdater {
    public static final double MAX_WIDTH_RATE = 0.75;
    private final Random random = new Random();
    private final Minecraft minecraft;
    private final Profiler profiler;
    private long value = 0L;

    public AviationDashboard(Minecraft minecraft) {
        this.minecraft = Objects.requireNonNull(minecraft);
        profiler = Objects.requireNonNull(minecraft.profiler);
    }

    @Override
    public void render(float partialTicks, boolean inGame) {
        if (inGame) {
            String text = "Elytra Dashboard: " + value;
            FontRenderer fontRenderer = minecraft.fontRenderer;
            Color color = minecraft.isGamePaused() ? Color.WHITE.darker() : Color.WHITE;
            ScaledResolution resolution = new ScaledResolution(minecraft);
            int txtWidth = fontRenderer.getStringWidth(text);
            int displayWidth = resolution.getScaledWidth();
            int posX = (displayWidth - txtWidth) / 2;
            int maxWidth = (int) (displayWidth * MAX_WIDTH_RATE);
            int displayHeight = resolution.getScaledHeight();
            if (txtWidth > maxWidth) {
                int height = fontRenderer.getWordWrappedHeight(text, maxWidth);
                int posY = (displayHeight - height);
                posX = (displayWidth - maxWidth) / 2;
                fontRenderer.drawSplitString(text, posX, posY, maxWidth, color.getRGB());
            } else {
                int posY = (displayHeight - fontRenderer.FONT_HEIGHT) / 4;
                fontRenderer.drawString(text, posX, posY, color.getRGB());
            }
        }
    }

    @Override
    public void tick(boolean inGame) {
        if (inGame) {
            value = random.nextLong();
        }
    }

    @Nonnull
    @Override
    public Profiler getProfiler() {
        return profiler;
    }
}

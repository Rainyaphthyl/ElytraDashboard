package io.github.rainyaphthyl.elytradashboard.display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Objects;

public class ElytraReporter implements FrameUpdater {
    public static final double MAX_WIDTH_RATE = 0.75;
    private final Minecraft minecraft;
    private final Profiler profiler;
    private final ElytraPacket packet = new ElytraPacket();
    private boolean tickValid = false;

    public ElytraReporter(Minecraft minecraft) {
        this.minecraft = Objects.requireNonNull(minecraft);
        profiler = Objects.requireNonNull(minecraft.profiler);
    }

    @Override
    public void render(float partialTicks, boolean inGame) {
        if (inGame && tickValid) {
            EntityPlayerSP player = minecraft.player;
            if (player != null && player.isElytraFlying()) {
                float reducedFallingDamage = packet.getReducedFallingDamage();
                float reducedCollisionDamage = packet.getReducedCollisionDamage();
                String text = String.format("Collision: %.1f / %.1f ; Falling: %.1f / %.1f",
                        packet.getCompleteCollisionDamage(), reducedCollisionDamage,
                        packet.getCompleteFallingDamage(), reducedFallingDamage);
                FontRenderer fontRenderer = minecraft.fontRenderer;
                int colorRGB = Color.WHITE.getRGB();
                float health = player.getHealth();
                if (reducedFallingDamage >= health || reducedCollisionDamage >= health) {
                    colorRGB = Color.RED.getRGB();
                }
                ScaledResolution resolution = new ScaledResolution(minecraft);
                int txtWidth = fontRenderer.getStringWidth(text);
                int displayWidth = resolution.getScaledWidth();
                int posX = (displayWidth - txtWidth) / 2;
                int maxWidth = (int) (displayWidth * MAX_WIDTH_RATE);
                int displayHeight = resolution.getScaledHeight();
                if (txtWidth > maxWidth) {
                    int height = fontRenderer.getWordWrappedHeight(text, maxWidth);
                    int posY = (displayHeight - height) / 4;
                    posX = (displayWidth - maxWidth) / 2;
                    fontRenderer.drawSplitString(text, posX, posY, maxWidth, colorRGB);
                } else {
                    int posY = (displayHeight - fontRenderer.FONT_HEIGHT) / 4;
                    fontRenderer.drawString(text, posX, posY, colorRGB);
                }
            }
        }
    }

    @Override
    public void tick(boolean inGame) {
        if (inGame) {
            EntityPlayerSP player = minecraft.player;
            if (player != null && player.isElytraFlying()) {
                double motionX = player.motionX;
                double motionZ = player.motionZ;
                double motionHorizon = Math.sqrt(motionX * motionX + motionZ * motionZ);
                packet.setCompleteCollisionDamage((float) (motionHorizon * 10.0 - 3.0));
                if (player.fallDistance > 0.0F) {
                    packet.setCompleteFallingDamage((float) MathHelper.ceil(player.fallDistance - 3.0F));
                } else {
                    packet.setCompleteFallingDamage(0.0F);
                }
                packet.applyReducedDamages(player.getArmorInventoryList());
            }
            tickValid = true;
        } else {
            tickValid = false;
        }
    }

    @Nonnull
    @Override
    public Profiler getProfiler() {
        return profiler;
    }
}

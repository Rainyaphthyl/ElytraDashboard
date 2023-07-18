package io.github.rainyaphthyl.elytradashboard.display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class FlightInstrument {
    public static final double MAX_WIDTH_RATE = 0.75;
    /**
     * Seems that it should use the client data...
     */
    public static final boolean USING_SERVER_DATA = false;
    private final ElytraPacket packet = new ElytraPacket();
    private final AtomicReferenceArray<EntityPlayer> playerCache = new AtomicReferenceArray<>(2);
    private float health = 0.0F;
    private boolean tickValid = false;

    public FlightInstrument() {
    }

    public EntityPlayer requestServerSinglePlayer(@Nonnull Minecraft minecraft) {
        EntityPlayerSP playerSP = minecraft.player;
        if (USING_SERVER_DATA && minecraft.isSingleplayer()) {
            synchronized (playerCache) {
                if (playerCache.get(0) == playerSP) {
                    return playerCache.get(1);
                } else {
                    MinecraftServer server = minecraft.getIntegratedServer();
                    if (server != null) {
                        UUID uuid = playerSP.getUniqueID();
                        PlayerList playerList = server.getPlayerList();
                        EntityPlayerMP playerMP = playerList.getPlayerByUUID(uuid);
                        //noinspection ConstantValue
                        if (playerMP != null && uuid.equals(playerMP.getUniqueID())) {
                            playerCache.set(0, playerSP);
                            playerCache.set(1, playerMP);
                            return playerMP;
                        }
                    }
                }
            }
        }
        return playerSP;
    }

    /**
     * Queries the variables of movement.
     *
     * @param minecraft The minecraft client instance
     * @param inGame    {@code true} if in a world or a server, {@code false} if in the main menu
     */
    public void tick(@Nonnull Minecraft minecraft, boolean inGame) {
        if (inGame) {
            EntityPlayerSP playerSP = minecraft.player;
            if (playerSP != null && playerSP.isElytraFlying()) {
                EntityPlayer player = requestServerSinglePlayer(minecraft);
                if (player != null && player.isElytraFlying()) {
                    double motionX = player.motionX;
                    double motionZ = player.motionZ;
                    double motionHorizon = Math.sqrt(motionX * motionX + motionZ * motionZ);
                    packet.setCompleteCollisionDamage((float) (motionHorizon * 10.0 - 3.0));
                    float fallDistance = player.fallDistance;
                    if (fallDistance > 0.0F) {
                        packet.setCompleteFallingDamage((float) MathHelper.ceil(fallDistance - 3.0F));
                    } else {
                        packet.setCompleteFallingDamage(0.0F);
                    }
                    packet.applyReducedDamages(player.getArmorInventoryList());
                    health = player.getHealth();
                    tickValid = true;
                    return;
                }
            }
        }
        if (tickValid) {
            tickValid = false;
        }
    }

    public void render(@Nonnull Minecraft minecraft, boolean inGame) {
        if (inGame && tickValid) {
            float reducedFallingDamage = packet.getReducedFallingDamage();
            float reducedCollisionDamage = packet.getReducedCollisionDamage();
            String text = String.format("Collision: %.1f / %.1f ; Falling: %.1f / %.1f",
                    packet.getCompleteCollisionDamage(), reducedCollisionDamage,
                    packet.getCompleteFallingDamage(), reducedFallingDamage);
            FontRenderer fontRenderer = minecraft.fontRenderer;
            int colorRGB = Color.WHITE.getRGB();
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

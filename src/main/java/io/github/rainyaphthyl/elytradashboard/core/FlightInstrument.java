package io.github.rainyaphthyl.elytradashboard.core;

import io.github.rainyaphthyl.elytradashboard.core.record.EnumRW;
import io.github.rainyaphthyl.elytradashboard.core.record.FireworkPacket;
import io.github.rainyaphthyl.elytradashboard.core.record.InstantPacket;
import io.github.rainyaphthyl.elytradashboard.util.GenericHelper;
import io.github.rainyaphthyl.elytradashboard.util.InfoLineRecord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class FlightInstrument {
    public static final double MAX_WIDTH_RATE = 0.75;
    /**
     * Seems that it should use the client data...
     */
    public static final boolean USING_SERVER_DATA = false;
    private final InstantPacket instantPacket = new InstantPacket();
    private final FireworkPacket fireworkPacket = new FireworkPacket();
    private final TripPacket tripPacket = new TripPacket();
    private final AtomicReferenceArray<EntityPlayer> playerCache = new AtomicReferenceArray<>(2);
    private long initTripTick = 0L;
    private long tripDuration = 0L;
    private boolean duringFlight = false;

    private static void renderInfoLines(@Nonnull Minecraft minecraft, @Nonnull List<Tuple<String, Color>> textList) {
        FontRenderer fontRenderer = minecraft.fontRenderer;
        ScaledResolution resolution = new ScaledResolution(minecraft);
        List<InfoLineRecord> displayedList = new ArrayList<>();
        final int displayWidth = resolution.getScaledWidth();
        final int displayHeight = resolution.getScaledHeight();
        final int maxWidth = (int) (displayWidth * MAX_WIDTH_RATE);
        int posDeltaX = 0;
        int posDeltaY = 0;
        int totalWidth = 0;
        int totalHeight = 0;
        // check block height of info lines
        for (Tuple<String, Color> tuple : textList) {
            String text = tuple.getFirst();
            int txtWidth = fontRenderer.getStringWidth(text);
            int txtHeight = fontRenderer.FONT_HEIGHT;
            boolean split = txtWidth > maxWidth;
            if (split) {
                txtWidth = maxWidth;
                txtHeight = fontRenderer.getWordWrappedHeight(text, maxWidth);
            }
            totalHeight += txtHeight;
            if (txtWidth > totalWidth) {
                totalWidth = txtWidth;
            }
            Color color = tuple.getSecond();
            displayedList.add(new InfoLineRecord(text, posDeltaX, posDeltaY, color.getRGB(), split));
            posDeltaY += txtHeight;
        }
        // draw strings
        int posGlobalX = (displayWidth - totalWidth) / 2;
        int posGlobalY = displayHeight / 3 - totalHeight / 2;
        for (InfoLineRecord record : displayedList) {
            int posX = posGlobalX + record.posDeltaX;
            int posY = posGlobalY + record.posDeltaY;
            if (record.split) {
                fontRenderer.drawSplitString(record.text, posX, posY, maxWidth, record.color);
            } else {
                fontRenderer.drawString(record.text, posX, posY, record.color);
            }
        }
    }

    @Nonnull
    private static Integer increase(Byte key, Integer value) {
        return value == null ? 1 : value + 1;
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
                        if (GenericHelper.equalsUnique(playerSP, playerMP)) {
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
        if (inGame && !(minecraft.currentScreen instanceof GuiGameOver)) {
            EntityPlayerSP playerSP = minecraft.player;
            if (playerSP != null && playerSP.isElytraFlying()) {
                EntityPlayer player = requestServerSinglePlayer(minecraft);
                if (player == playerSP || player != null && player.isElytraFlying()) {
                    long currTripTick = minecraft.world.getTotalWorldTime();
                    if (!duringFlight) {
                        tripPacket.setInitPosition(player.posX, player.posY, player.posZ);
                        initTripTick = currTripTick;
                        duringFlight = true;
                    }
                    tripDuration = currTripTick - initTripTick;
                    updateElytraData(player);
                    tripPacket.updateVelocity(player.posX, player.posY, player.posZ, tripDuration);
                    return;
                }
            }
        }
        stopFlight();
    }

    private void updateElytraData(@Nonnull EntityPlayer player) {
        double motionX = player.motionX;
        double motionZ = player.motionZ;
        double motionHorizon = Math.sqrt(motionX * motionX + motionZ * motionZ);
        instantPacket.setCompleteCollisionDamage((float) (motionHorizon * 10.0 - 3.0));
        float fallDistance = player.fallDistance;
        if (fallDistance > 0.0F) {
            instantPacket.setCompleteFallingDamage((float) MathHelper.ceil(fallDistance - 3.0F));
        } else {
            instantPacket.setCompleteFallingDamage(0.0F);
        }
        instantPacket.applyReducedDamages(player.getArmorInventoryList());
        instantPacket.health = player.getHealth();
    }

    private void stopFlight() {
        if (duringFlight) {
            duringFlight = false;
            fireworkPacket.runSyncTask(EnumRW.WRITE, this::resetFirework);
        }
    }

    public void asyncRecordFirework(byte level) {
        Runnable task = () -> fireworkPacket.runSyncTask(EnumRW.WRITE, () -> {
            fireworkPacket.levelMap.compute(level, FlightInstrument::increase);
            fireworkPacket.fuelCount.addAndGet(level);
            fireworkPacket.fuelError.compareAndSet(false, level < 0);
        });
        Thread thread = new Thread(task, "Firework Logger");
        thread.setDaemon(true);
        thread.start();
    }

    public void render(@Nonnull Minecraft minecraft, boolean inGame) {
        if (inGame && duringFlight) {
            float reducedCollisionDamage = instantPacket.getReducedCollisionDamage();
            String text = String.format("Collision Damage: %.2f / %.2f", instantPacket.getCompleteCollisionDamage(), reducedCollisionDamage);
            Color color = reducedCollisionDamage >= instantPacket.health ? Color.RED : Color.WHITE;
            List<Tuple<String, Color>> textList = new ArrayList<>();
            textList.add(new Tuple<>(text, color));
            float reducedFallingDamage = instantPacket.getReducedFallingDamage();
            color = reducedFallingDamage >= instantPacket.health ? Color.RED : Color.WHITE;
            text = String.format("Falling Damage: %.2f / %.2f", instantPacket.getCompleteFallingDamage(), reducedFallingDamage);
            textList.add(new Tuple<>(text, color));
            textList.add(new Tuple<>("Flight Duration: " + tripDuration, Color.WHITE));
            AtomicInteger poolInt = new AtomicInteger(0);
            AtomicReference<Color> poolColor = new AtomicReference<>(null);
            fireworkPacket.runSyncTask(EnumRW.READ, () -> {
                poolInt.set(fireworkPacket.fuelCount.get());
                poolColor.set(fireworkPacket.fuelError.get() ? Color.LIGHT_GRAY : Color.WHITE);
            });
            textList.add(new Tuple<>("Firework Fuels: " + poolInt.get(), poolColor.get()));
            double displacement = tripPacket.getHorizonDisplacement();
            double velocity = tripPacket.getHorizonVelocity();
            double distance = tripPacket.getHorizonDistance();
            double speed = tripPacket.getHorizonSpeed();
            double fuelEfficiency = (double) poolInt.get() / distance;
            textList.add(new Tuple<>(String.format("Avg Fuel Efficiency: %.2f GPD/km", fuelEfficiency * 1000), Color.WHITE));
            textList.add(new Tuple<>(String.format("Avg XZ Velocity: %.2f m/s", velocity * 20.0), Color.WHITE));
            textList.add(new Tuple<>(String.format("Total XZ Displacement: %.0f m", displacement), Color.WHITE));
            textList.add(new Tuple<>(String.format("Avg XZ Speed: %.2f m/s", speed * 20.0), Color.WHITE));
            textList.add(new Tuple<>(String.format("Total XZ Distance: %.0f m", distance), Color.WHITE));
            renderInfoLines(minecraft, textList);
        }
    }

    private void resetFirework() {
        fireworkPacket.levelMap.clear();
        fireworkPacket.fuelCount.set(0);
        fireworkPacket.fuelError.set(false);
    }
}

package io.github.rainyaphthyl.elytradashboard.core;

import io.github.rainyaphthyl.elytradashboard.core.record.CumulativePacket;
import io.github.rainyaphthyl.elytradashboard.core.record.EnumRW;
import io.github.rainyaphthyl.elytradashboard.core.record.InstantPacket;
import io.github.rainyaphthyl.elytradashboard.core.record.LockRunner;
import io.github.rainyaphthyl.elytradashboard.util.GenericHelper;
import io.github.rainyaphthyl.elytradashboard.util.InfoLineRecord;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

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
    public static final Integer COLOR_NORMAL = Color.WHITE.getRGB() & 0x90FFFFFF;
    public static final Integer COLOR_BG = 0x90505050;
    public static final Integer COLOR_WARNING = Color.YELLOW.getRGB() & 0x90FFFFFF;
    public static final Integer COLOR_ERROR = Color.RED.getRGB() & 0x90FFFFFF;
    private final InstantPacket instantPacket = new InstantPacket();
    private final CumulativePacket cumulativePacket = new CumulativePacket();
    private final AtomicReferenceArray<EntityPlayer> playerCache = new AtomicReferenceArray<>(2);
    private final LockRunner fireworkLock = new LockRunner(true);
    private boolean duringFlight = false;

    private static void renderInfoLines(@Nonnull Minecraft minecraft, @Nonnull List<Tuple<String, Integer>> textList) {
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
        for (Tuple<String, Integer> tuple : textList) {
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
            int color = tuple.getSecond();
            displayedList.add(new InfoLineRecord(text, posDeltaX, posDeltaY, color, split));
            posDeltaY += txtHeight;
        }
        // draw strings
        int posGlobalX = (displayWidth - totalWidth) / 2;
        int posGlobalY = displayHeight / 100;
        Gui.drawRect(posGlobalX - 1, posGlobalY - 1, posGlobalX + totalWidth, posGlobalY + totalHeight, COLOR_BG);
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
                        cumulativePacket.setInitPosition(player.posX, player.posY, player.posZ);
                        cumulativePacket.initTripTick = currTripTick;
                        duringFlight = true;
                    }
                    cumulativePacket.tripDuration = currTripTick - cumulativePacket.initTripTick;
                    updateElytraData(player);
                    cumulativePacket.updateVelocity(player.posX, player.posY, player.posZ);
                    updateHeight(player);
                    return;
                }
            }
        }
        stopFlight();
    }

    private void updateHeight(@Nonnull EntityPlayer player) {
        double altitude = player.posY;
        double groundLevel = -64.0;
        World world = player.getEntityWorld();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(
                MathHelper.floor(player.posX),
                Math.min(MathHelper.floor(player.posY), world.getHeight()),
                MathHelper.floor(player.posZ)
        );
        AxisAlignedBB playerBox = player.getEntityBoundingBox();
        boolean flag = true;
        for (int y = blockPos.getY(); flag && y > 0; --y) {
            int blockMinX = MathHelper.floor(playerBox.minX);
            int blockMinZ = MathHelper.floor(playerBox.minZ);
            int blockMaxX = MathHelper.floor(playerBox.maxX);
            int blockMaxZ = MathHelper.floor(playerBox.maxZ);
            List<AxisAlignedBB> bbList = new ArrayList<>();
            AxisAlignedBB maskBox = new AxisAlignedBB(
                    playerBox.minX, y, playerBox.minZ, playerBox.maxX, y + 1, playerBox.maxZ
            );
            for (int x = blockMinX; x <= blockMaxX; ++x) {
                for (int z = blockMinZ; z <= blockMaxZ; ++z) {
                    blockPos.setPos(x, y, z);
                    IBlockState blockState = world.getBlockState(blockPos);
                    blockState.addCollisionBoxToList(world, blockPos, maskBox, bbList, null, false);
                }
            }
            for (AxisAlignedBB boundingBox : bbList) {
                double tempY = boundingBox.maxY;
                if (tempY > groundLevel) {
                    groundLevel = tempY;
                    flag = false;
                }
            }
        }
        instantPacket.updateHeight(altitude, groundLevel);
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
            fireworkLock.runSyncTask(EnumRW.WRITE, this::resetFirework);
        }
    }

    public void recordFirework(byte level) {
        fireworkLock.runSyncTask(EnumRW.WRITE, () -> {
            cumulativePacket.levelMap.compute(level, FlightInstrument::increase);
            cumulativePacket.fuelCount.addAndGet(level);
            cumulativePacket.fuelError.compareAndSet(false, level < 0);
        });
    }

    public void render(@Nonnull Minecraft minecraft, boolean inGame) {
        if (inGame && duringFlight) {
            float reducedCollisionDamage = instantPacket.getReducedCollisionDamage();
            String text = String.format("Collision Damage: %.2f / %.2f", instantPacket.getCompleteCollisionDamage(), reducedCollisionDamage);
            Integer color = reducedCollisionDamage >= instantPacket.health ? COLOR_WARNING : COLOR_NORMAL;
            List<Tuple<String, Integer>> textList = new ArrayList<>();
            textList.add(new Tuple<>(text, color));
            float reducedFallingDamage = instantPacket.getReducedFallingDamage();
            color = reducedFallingDamage >= instantPacket.health ? COLOR_WARNING : COLOR_NORMAL;
            text = String.format("Falling Damage: %.2f / %.2f", instantPacket.getCompleteFallingDamage(), reducedFallingDamage);
            textList.add(new Tuple<>(text, color));
            double altitude = instantPacket.getAltitude();
            double height = instantPacket.getHeight();
            text = String.format("Altitude: %.2f (%.2f to %.2f)", altitude, height, instantPacket.getGroundLevel());
            color = height < 100.0 ? COLOR_WARNING : COLOR_NORMAL;
            textList.add(new Tuple<>(text, color));
            textList.add(new Tuple<>("----------------", COLOR_NORMAL));
            textList.add(new Tuple<>("Flight Duration: " + cumulativePacket.tripDuration, COLOR_NORMAL));
            AtomicInteger poolNum = new AtomicInteger(0);
            AtomicReference<Integer> poolColor = new AtomicReference<>(0);
            fireworkLock.runSyncTask(EnumRW.READ, () -> {
                poolNum.set(cumulativePacket.fuelCount.get());
                poolColor.set(cumulativePacket.fuelError.get() ? COLOR_ERROR : COLOR_NORMAL);
            });
            textList.add(new Tuple<>("Firework Fuels: " + poolNum.get(), poolColor.get()));
            double displacement = cumulativePacket.getTotalDisplacement();
            double velocity = cumulativePacket.getTotalVelocity();
            double distance = cumulativePacket.getHorizonDistance();
            double speed = cumulativePacket.getHorizonSpeed();
            double fuelEffTotal = (double) poolNum.get() / displacement;
            double fuelEffHorizon = (double) poolNum.get() / distance;
            textList.add(new Tuple<>(String.format("Avg XYZ Fuel Efficiency: %.2f G/km", fuelEffTotal * 1000), poolColor.get()));
            textList.add(new Tuple<>(String.format("Avg XYZ Velocity: %.2f m/s", velocity * 20.0), COLOR_NORMAL));
            textList.add(new Tuple<>(String.format("Total XYZ Displacement: %.0f m", displacement), COLOR_NORMAL));
            textList.add(new Tuple<>(String.format("Avg XZ Fuel Efficiency: %.2f G/km", fuelEffHorizon * 1000), poolColor.get()));
            textList.add(new Tuple<>(String.format("Avg XZ Speed: %.2f m/s", speed * 20.0), COLOR_NORMAL));
            textList.add(new Tuple<>(String.format("Total XZ Distance: %.0f m", distance), COLOR_NORMAL));
            renderInfoLines(minecraft, textList);
        }
    }

    private void resetFirework() {
        cumulativePacket.levelMap.clear();
        cumulativePacket.fuelCount.set(0);
        cumulativePacket.fuelError.set(false);
    }
}

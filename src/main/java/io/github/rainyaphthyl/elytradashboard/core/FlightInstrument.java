package io.github.rainyaphthyl.elytradashboard.core;

import io.github.rainyaphthyl.elytradashboard.config.ModSettings;
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
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
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
    private String currWarningTitle = null;
    private boolean duringFlight = false;

    private static void renderInfoLines(@Nonnull Minecraft minecraft, @Nonnull List<Tuple<String, Integer>> textList, boolean transparent) {
        FontRenderer fontRenderer = minecraft.fontRenderer;
        ScaledResolution resolution = new ScaledResolution(minecraft);
        List<InfoLineRecord> displayedList = new ArrayList<>();
        final int displayWidth = resolution.getScaledWidth();
        //final int displayHeight = resolution.getScaledHeight();
        final int maxWidth = (int) (displayWidth * MAX_WIDTH_RATE);
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
            displayedList.add(new InfoLineRecord(text, txtWidth, txtHeight, color, split));
        }
        // draw strings
        int posGlobalX;
        final int posGlobalY = 1;
        switch (ModSettings.INSTANCE.dashboardPosX) {
            case LEFT:
                posGlobalX = 1;
                break;
            case RIGHT:
                posGlobalX = displayWidth - totalWidth;
                break;
            case MID:
            default:
                posGlobalX = (displayWidth - totalWidth) / 2;
        }
        GlStateManager.pushMatrix();
        int actualColor = COLOR_BG;
        if (transparent) {
            int rgb = actualColor & 0x00FFFFFF;
            actualColor = rgb | 0x40000000;
        }
        Gui.drawRect(posGlobalX - 1, posGlobalY - 1, posGlobalX + totalWidth, posGlobalY + totalHeight, actualColor);
        int renderHeight = 0;
        for (InfoLineRecord record : displayedList) {
            int posY = posGlobalY + renderHeight;
            actualColor = record.color;
            if (transparent) {
                int rgb = actualColor & 0x00FFFFFF;
                actualColor = rgb | 0x40000000;
            }
            if (record.split) {
                fontRenderer.drawSplitString(record.text, posGlobalX, posY, maxWidth, actualColor);
            } else {
                fontRenderer.drawString(record.text, posGlobalX, posY, actualColor);
            }
            renderHeight += record.txtHeight;
        }
        GlStateManager.popMatrix();
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
                    updateHeight(player);
                    updateElytraData(player);
                    cumulativePacket.updateVelocity(player.posX, player.posY, player.posZ);
                    if (ModSettings.INSTANCE.warningEnabled) {
                        if (instantPacket.getHeight() < 100.0 && instantPacket.getReducedPotentialCrash() > instantPacket.health) {
                            // terrain, pull up
                            checkWarning(minecraft, player);
                            currWarningTitle = "PULL UP!";
                        } else {
                            currWarningTitle = null;
                        }
                    }
                    return;
                }
            }
        }
        stopFlight();
    }

    private void checkWarning(@Nonnull Minecraft minecraft, @Nonnull EntityPlayer player) {
        minecraft.addScheduledTask(() -> {
            NetHandlerPlayClient connection = minecraft.getConnection();
            if (connection != null) {
                SoundEvent soundEvent = SoundEvent.REGISTRY.getObject(new ResourceLocation("block.note.bell"));
                if (soundEvent != null) {
                    connection.handleSoundEffect(new SPacketSoundEffect(soundEvent, SoundCategory.VOICE, player.posX, player.posY, player.posZ, 4.0F, 0.5F));
                }
            }
        });
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
        float currFallDamage = fallDistance - 3.0F;
        if (currFallDamage >= 0.0F) {
            instantPacket.setCompleteFallingDamage((float) MathHelper.ceil(currFallDamage));
            instantPacket.setCompletePotentialCrash((float) MathHelper.ceil(currFallDamage + (float) getHeight()));
        } else {
            instantPacket.setCompleteFallingDamage(0.0F);
            instantPacket.setCompletePotentialCrash(0.0F);
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
        Runnable task = () -> fireworkLock.runSyncTask(EnumRW.WRITE, () -> {
            cumulativePacket.levelMap.compute(level, FlightInstrument::increase);
            cumulativePacket.fuelCount.addAndGet(level);
            cumulativePacket.fuelError.compareAndSet(false, level < 0);
        });
        Thread thread = new Thread(task, "Firework Logger");
        thread.setDaemon(true);
        thread.start();
    }

    public void renderDashboard(@Nonnull Minecraft minecraft, boolean inGame) {
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
            float reducedPotentialCrash = instantPacket.getReducedPotentialCrash();
            color = reducedPotentialCrash >= instantPacket.health ? COLOR_WARNING : COLOR_NORMAL;
            text = String.format("Potential Crash: %.2f / %.2f", instantPacket.getCompletePotentialCrash(), reducedPotentialCrash);
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
            textList.add(new Tuple<>(String.format("Avg XYZ Fuel Efficiency: %.2f g/km", fuelEffTotal * 1000), poolColor.get()));
            textList.add(new Tuple<>(String.format("Avg XYZ Velocity: %.2f m/s", velocity * 20.0), COLOR_NORMAL));
            textList.add(new Tuple<>(String.format("Total XYZ Displacement: %.0f m", displacement), COLOR_NORMAL));
            textList.add(new Tuple<>(String.format("Avg XZ Fuel Efficiency: %.2f g/km", fuelEffHorizon * 1000), poolColor.get()));
            textList.add(new Tuple<>(String.format("Avg XZ Speed: %.2f m/s", speed * 20.0), COLOR_NORMAL));
            textList.add(new Tuple<>(String.format("Total XZ Distance: %.0f m", distance), COLOR_NORMAL));
            boolean lowAltitude = altitude < minecraft.world.getHeight();
            renderInfoLines(minecraft, textList, lowAltitude);
        }
    }

    public void renderWarning(@Nonnull Minecraft minecraft, boolean inGame) {
        if (inGame && duringFlight) {
            if (currWarningTitle != null) {
                int halfWidth = minecraft.fontRenderer.getStringWidth(currWarningTitle) / 2;
                int halfHeight = minecraft.fontRenderer.FONT_HEIGHT / 2;
                FontRenderer fontRenderer = minecraft.fontRenderer;
                GlStateManager.pushMatrix();
                ScaledResolution resolution = new ScaledResolution(minecraft);
                int rangeWidth = resolution.getScaledWidth() >> 1;
                int rangeHeight = resolution.getScaledHeight() >> 1;
                GlStateManager.translate((float) rangeWidth, (float) rangeHeight, 0.0F);
                GlStateManager.scale(4.0F, 4.0F, 4.0F);
                Gui.drawRect(-rangeWidth, -halfHeight - 1, rangeWidth, halfHeight, 0x40FF0000);
                fontRenderer.drawString(currWarningTitle, -halfWidth, -halfHeight, 0xFFFFFF00);
                GlStateManager.popMatrix();
            }
        }
    }

    private void resetFirework() {
        cumulativePacket.levelMap.clear();
        cumulativePacket.fuelCount.set(0);
        cumulativePacket.fuelError.set(false);
    }

    public double getHeight() {
        return instantPacket.getHeight();
    }
}

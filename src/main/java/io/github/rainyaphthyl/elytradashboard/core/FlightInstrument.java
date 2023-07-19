package io.github.rainyaphthyl.elytradashboard.core;

import io.github.rainyaphthyl.elytradashboard.mixin.AccessEntityFireworkRocket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class FlightInstrument {
    /**
     * {@code flightDuration} is ordinarily 1, 2, or 3, but it is actually stored in a byte tag, up to 127?
     * <pre>
     * {@code Random rand = new Random();
     * int lifetime = 10 * (1 + flightDuration) + rand.nextInt(6) + rand.nextInt(7);}
     * </pre>
     */
    public static final int MAX_FIREWORK_LIFETIME = 10 * (1 + Byte.MAX_VALUE) + 5 + 6;
    public static final double MAX_WIDTH_RATE = 0.75;
    /**
     * Seems that it should use the client data...
     */
    public static final boolean USING_SERVER_DATA = false;
    private static final AtomicReference<Thread> threadCache = new AtomicReference<>();
    private final ElytraPacket packet = new ElytraPacket();
    private final AtomicReferenceArray<EntityPlayer> playerCache = new AtomicReferenceArray<>(2);
    /**
     * key: {@link UUID} - the UUID of the rocket
     * <p>
     * value: {@link Long} - the tick time when rocket is tracked, used for TTL cache
     */
    private final Map<UUID, Long> fireworkTickCache = new LinkedHashMap<>();
    /**
     * key: {@link Integer} - the actual lifetime of the rocket
     * <p>
     * value: {@link Integer} - the number of rockets with that lifetime
     */
    private final Map<Integer, Integer> fireworkLifetimeRecord = new HashMap<>();
    private long initTripTick = 0L;
    private long currTripTick = 0L;
    private float health = 0.0F;
    private boolean duringFlight = false;

    private static void printCurrentThread(String task) {
        synchronized (threadCache) {
            Thread thread = Thread.currentThread();
            Thread prev = threadCache.get();
            if (prev != thread) {
                String message = task + ": " + thread + "@" + thread.hashCode();
                threadCache.set(thread);
                System.out.println(message);
                NetHandlerPlayClient connection = Minecraft.getMinecraft().getConnection();
                if (connection != null) {
                    connection.handleChat(new SPacketChat(new TextComponentString(message)));
                }
            }
        }
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
        if (inGame && !(minecraft.currentScreen instanceof GuiGameOver)) {
            EntityPlayerSP playerSP = minecraft.player;
            if (playerSP != null && playerSP.isElytraFlying()) {
                EntityPlayer player = requestServerSinglePlayer(minecraft);
                if (player != null && player.isElytraFlying()) {
                    printCurrentThread("tick");
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
                    currTripTick = minecraft.world.getTotalWorldTime();
                    if (!duringFlight) {
                        initTripTick = currTripTick;
                        duringFlight = true;
                    }
                    return;
                }
            }
        }
        stopFlight();
    }

    public void stopFlight() {
        if (duringFlight) {
            duringFlight = false;
            fireworkTickCache.clear();
            fireworkLifetimeRecord.clear();
        }
    }

    public void markFireworkUsage(UUID uuid, int lifetime) {
        printCurrentThread("markFireworkUsage");
        if (!fireworkTickCache.containsKey(uuid)) {
            fireworkTickCache.put(uuid, currTripTick);
            Integer key = lifetime;
            Integer value = fireworkLifetimeRecord.get(key);
            if (value == null) {
                value = 0;
            }
            ++value;
            fireworkLifetimeRecord.put(key, value);
        }
        Set<Map.Entry<UUID, Long>> entrySet = fireworkTickCache.entrySet();
        Iterator<Map.Entry<UUID, Long>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            Long value = entry.getValue();
            if (value == null || currTripTick - value > MAX_FIREWORK_LIFETIME) {
                iterator.remove();
            } else {
                break;
            }
        }
    }

    public void checkMarkFirework(EntityPlayer player, Entity entity) {
        if (entity instanceof EntityFireworkRocket && player instanceof EntityPlayerSP) {
            EntityFireworkRocket firework = (EntityFireworkRocket) entity;
            if (firework instanceof AccessEntityFireworkRocket) {
                EntityLivingBase payload = ((AccessEntityFireworkRocket) firework).getBoostedEntity();
                if (payload instanceof EntityPlayer) {
                    UUID uuidHost = player.getUniqueID();
                    UUID uuidBoosted = payload.getUniqueID();
                    if (uuidBoosted.equals(uuidHost)) {
                        int lifetime = ((AccessEntityFireworkRocket) firework).getLifetime();
                        UUID uuidRocket = firework.getUniqueID();
                        markFireworkUsage(uuidRocket, lifetime);
                    }
                }
            }
        }
    }

    public void render(@Nonnull Minecraft minecraft, boolean inGame) {
        if (inGame && duringFlight) {
            printCurrentThread("render");
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

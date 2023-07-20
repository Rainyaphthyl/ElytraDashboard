package io.github.rainyaphthyl.elytradashboard.core;

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
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
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
    private final ConcurrentMap<Integer, Integer> fireworkLifetimeRecord = new ConcurrentHashMap<>();
    private final AtomicLong fireworkTotalTime = new AtomicLong(0L);
    private long initTripTick = 0L;
    private long currTripTick = 0L;
    private float health = 0.0F;
    private boolean duringFlight = false;

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
            synchronized (fireworkTickCache) {
                fireworkTickCache.clear();
            }
            synchronized (fireworkLifetimeRecord) {
                fireworkLifetimeRecord.clear();
                fireworkTotalTime.set(0L);
            }
        }
    }

    public void markFireworkUsage(UUID uuid, int lifetime) {
        Integer lifetimeObj = lifetime;
        boolean novel = false;
        synchronized (fireworkTickCache) {
            if (!fireworkTickCache.containsKey(uuid)) {
                novel = true;
                Long tickObj = currTripTick;
                fireworkTickCache.put(uuid, tickObj);
            }
        }
        if (novel) {
            fireworkLifetimeRecord.compute(lifetimeObj, this::appendFireworkRecord);
            synchronized (fireworkTickCache) {
                Set<Map.Entry<UUID, Long>> entrySet = fireworkTickCache.entrySet();
                Iterator<Map.Entry<UUID, Long>> iterator = entrySet.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<UUID, Long> entry = iterator.next();
                    Long timestamp = entry.getValue();
                    if (timestamp == null || currTripTick - timestamp > MAX_FIREWORK_LIFETIME) {
                        iterator.remove();
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public void render(@Nonnull Minecraft minecraft, boolean inGame) {
        if (inGame && duringFlight) {
            float reducedFallingDamage = packet.getReducedFallingDamage();
            float reducedCollisionDamage = packet.getReducedCollisionDamage();
            String text = String.format("Collision: %.1f / %.1f ; Falling: %.1f / %.1f",
                    packet.getCompleteCollisionDamage(), reducedCollisionDamage,
                    packet.getCompleteFallingDamage(), reducedFallingDamage);
            Color color;
            if (reducedFallingDamage >= health || reducedCollisionDamage >= health) {
                color = Color.RED;
            } else {
                color = Color.WHITE;
            }
            List<Tuple<String, Color>> textList = new ArrayList<>();
            textList.add(new Tuple<>(text, color));
            long totalFireworks = fireworkTotalTime.get();
            textList.add(new Tuple<>("Total Firework Lifetime: " + totalFireworks, Color.WHITE));
            long flightDuration = currTripTick - initTripTick;
            textList.add(new Tuple<>("Total Flight Duration: " + flightDuration, Color.WHITE));
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
                displayedList.add(new InfoLineRecord(tuple.getFirst(), posDeltaX, posDeltaY, tuple.getSecond().getRGB(), split));
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
    }

    @Nonnull
    private Integer appendFireworkRecord(@Nonnull Integer key, Integer prev) {
        int currValue = prev == null ? 1 : prev + 1;
        fireworkTotalTime.addAndGet(key);
        return currValue;
    }
}

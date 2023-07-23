package io.github.rainyaphthyl.elytradashboard.core.record;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FireworkPacket extends AsyncPacket {
    /**
     * Weighted fireworks usage
     */
    public final AtomicInteger fuelCount = new AtomicInteger(0);
    /**
     * Set to {@code true} if fireworks with negative gunpowder number are used
     */
    public final AtomicBoolean fuelError = new AtomicBoolean(false);
    public final Map<Byte, Integer> levelMap = new HashMap<>();
}

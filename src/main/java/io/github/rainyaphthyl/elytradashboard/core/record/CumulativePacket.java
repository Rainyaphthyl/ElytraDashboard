package io.github.rainyaphthyl.elytradashboard.core.record;

import io.github.rainyaphthyl.elytradashboard.util.Vec3dPool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CumulativePacket {
    /**
     * Weighted fireworks usage
     */
    public final AtomicInteger fuelCount = new AtomicInteger(0);
    /**
     * Set to {@code true} if fireworks with negative gunpowder number are used
     */
    public final AtomicBoolean fuelError = new AtomicBoolean(false);
    public final Map<Byte, Integer> levelMap = new HashMap<>();
    private final Vec3dPool initPos = new Vec3dPool();
    private final Vec3dPool currPos = new Vec3dPool();
    private final Vec3dPool displacement = new Vec3dPool();
    private final Vec3dPool velocity = new Vec3dPool();
    private final Vec3dPool prevPos = new Vec3dPool();
    public long initTripTick = 0L;
    public long tripDuration = 0L;
    private double totalVelocity = 0.0;
    private double totalDisplacement = 0.0;
    private double horizonDistance = 0.0;
    private double horizonSpeed = 0.0;

    public void updateVelocity(double posX, double posY, double posZ) {
        currPos.setValues(posX, posY, posZ);
        displacement.setValues(currPos.x - initPos.x, currPos.y - initPos.y, currPos.z - initPos.z);
        velocity.setValues(currPos.x / tripDuration, currPos.y / tripDuration, currPos.z / tripDuration);
        totalDisplacement = Math.sqrt(displacement.x * displacement.x + displacement.y * displacement.y + displacement.z * displacement.z);
        totalVelocity = totalDisplacement / tripDuration;
        double dx = currPos.x - prevPos.x;
        double dz = currPos.z - prevPos.z;
        double increment = Math.sqrt(dx * dx + dz * dz);
        horizonDistance += increment;
        prevPos.setValues(currPos);
        horizonSpeed = horizonDistance / tripDuration;
    }

    public void setInitPosition(double posX, double posY, double posZ) {
        initPos.setValues(posX, posY, posZ);
        prevPos.setValues(posX, posY, posZ);
        horizonDistance = 0.0;
    }

    public double getTotalVelocity() {
        return totalVelocity;
    }

    public double getTotalDisplacement() {
        return totalDisplacement;
    }

    public double getHorizonDistance() {
        return horizonDistance;
    }

    public double getHorizonSpeed() {
        return horizonSpeed;
    }
}

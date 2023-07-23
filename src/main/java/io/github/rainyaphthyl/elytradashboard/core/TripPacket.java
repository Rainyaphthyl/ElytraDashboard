package io.github.rainyaphthyl.elytradashboard.core;

import io.github.rainyaphthyl.elytradashboard.util.Vec3dPool;

public class TripPacket {
    public final Vec3dPool initPos = new Vec3dPool();
    public final Vec3dPool currPos = new Vec3dPool();
    public final Vec3dPool displacement = new Vec3dPool();
    public final Vec3dPool velocity = new Vec3dPool();
    private double horizonVelocity = 0.0;
    private double horizonDisplacement = 0.0;

    public void updateVelocity(double posX, double posY, double posZ, long duration) {
        currPos.setValues(posX, posY, posZ);
        displacement.setValues(currPos.x - initPos.x, currPos.y - initPos.y, currPos.z - initPos.z);
        velocity.setValues(currPos.x / duration, currPos.y / duration, currPos.z / duration);
        horizonDisplacement = Math.sqrt(displacement.x * displacement.x + displacement.z * displacement.z);
        horizonVelocity = horizonDisplacement / duration;
    }

    public double getHorizonVelocity() {
        return horizonVelocity;
    }

    public double getHorizonDisplacement() {
        return horizonDisplacement;
    }
}

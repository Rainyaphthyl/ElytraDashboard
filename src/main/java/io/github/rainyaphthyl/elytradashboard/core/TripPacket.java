package io.github.rainyaphthyl.elytradashboard.core;

import io.github.rainyaphthyl.elytradashboard.util.Vec3dPool;

public class TripPacket {
    private final Vec3dPool initPos = new Vec3dPool();
    private final Vec3dPool currPos = new Vec3dPool();
    private final Vec3dPool displacement = new Vec3dPool();
    private final Vec3dPool velocity = new Vec3dPool();
    private final Vec3dPool prevPos = new Vec3dPool();
    private double horizonVelocity = 0.0;
    private double horizonDisplacement = 0.0;
    private double horizonDistance = 0.0;
    private double horizonSpeed = 0.0;

    public void updateVelocity(double posX, double posY, double posZ, long duration) {
        currPos.setValues(posX, posY, posZ);
        displacement.setValues(currPos.x - initPos.x, currPos.y - initPos.y, currPos.z - initPos.z);
        velocity.setValues(currPos.x / duration, currPos.y / duration, currPos.z / duration);
        horizonDisplacement = Math.sqrt(displacement.x * displacement.x + displacement.z * displacement.z);
        horizonVelocity = horizonDisplacement / duration;
        double dx = currPos.x - prevPos.x;
        double dz = currPos.z - prevPos.z;
        double increment = Math.sqrt(dx * dx + dz * dz);
        horizonDistance += increment;
        prevPos.setValues(currPos);
        horizonSpeed = horizonDistance / duration;
    }

    public void setInitPosition(double posX, double posY, double posZ) {
        initPos.setValues(posX, posY, posZ);
        prevPos.setValues(posX, posY, posZ);
        horizonDistance = 0.0;
    }

    public double getHorizonVelocity() {
        return horizonVelocity;
    }

    public double getHorizonDisplacement() {
        return horizonDisplacement;
    }

    public double getHorizonDistance() {
        return horizonDistance;
    }

    public double getHorizonSpeed() {
        return horizonSpeed;
    }
}

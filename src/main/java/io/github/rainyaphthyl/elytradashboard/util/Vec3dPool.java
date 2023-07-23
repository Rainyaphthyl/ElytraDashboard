package io.github.rainyaphthyl.elytradashboard.util;

public class Vec3dPool {
    public double x;
    public double y;
    public double z;

    public Vec3dPool() {
        this(0.0, 0.0, 0.0);
    }

    public Vec3dPool(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setValues(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

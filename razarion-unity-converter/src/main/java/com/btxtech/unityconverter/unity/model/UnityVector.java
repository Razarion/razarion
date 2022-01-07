package com.btxtech.unityconverter.unity.model;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.MathHelper;

public class UnityVector {
    private double x;
    private double y;
    private double z;
    private double w;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public UnityVector x(double x) {
        setX(x);
        return this;
    }

    public UnityVector y(double y) {
        setY(y);
        return this;
    }

    public UnityVector z(double z) {
        setZ(z);
        return this;
    }

    public UnityVector w(double w) {
        setW(w);
        return this;
    }

    public UnityVector normalise() {
        double magnitude = Math.sqrt(x * x + y * y + z * z + w * w);
        UnityVector normalisedVector = new UnityVector();
        normalisedVector.setX(x / magnitude);
        normalisedVector.setY(y / magnitude);
        normalisedVector.setZ(z / magnitude);
        normalisedVector.setW(w / magnitude);
        return normalisedVector;
    }

    public static UnityVector createFromAxisAngle(Vertex normalizedAxis, double angle) {
        double halfAngle = angle / 2;
        double s = Math.sin(halfAngle);

        UnityVector unityVector = new UnityVector();
        unityVector.x = normalizedAxis.getX() * s;
        unityVector.y = normalizedAxis.getY() * s;
        unityVector.z = normalizedAxis.getZ() * s;
        unityVector.w = Math.cos(halfAngle);
        return unityVector;
    }

    public void quaternionMultiply(UnityVector b) {
        double ax = x;
        double ay = y;
        double az = z;
        double aw = w;
        double bx = b.x;
        double by = b.y;
        double bz = b.z;
        double bw = b.w;

        this.x = ax * bw + aw * bx + ay * bz - az * by;
        this.y = ay * bw + aw * by + az * bx - ax * bz;
        this.z = az * bw + aw * bz + ax * by - ay * bx;
        this.w = aw * bw - ax * bx - ay * by - az * bz;
    }

    public Vertex quaternion2Angles() {
        double yaw;
        double roll;
        double pitch;

        double sqw = w * w;
        double sqx = x * x;
        double sqy = y * y;
        double sqz = z * z;
        double unit = sqx + sqy + sqz + sqw; // if normalized is one, otherwise
        double test = x * y + z * w; // is correction factor

        if (test > 0.499 * unit) { // singularity at North Pole
            roll = 2 * Math.atan2(x, w);
            pitch = MathHelper.QUARTER_RADIANT;
            yaw = 0;
        } else if (test < -0.499 * unit) { // singularity at South Pole
            roll = -2 * Math.atan2(x, w);
            pitch = -MathHelper.QUARTER_RADIANT;
            yaw = 0;
        } else {
            roll = Math.atan2(2 * y * w - 2 * x * z, sqx - sqy - sqz + sqw); // roll or heading
            pitch = Math.asin(2 * test / unit); // pitch or attitude
            yaw = Math.atan2(2 * x * w - 2 * y * z, -sqx + sqy - sqz + sqw); // yaw or bank
        }
        return new Vertex(yaw, roll, pitch);
    }


    @Override
    public String toString() {
        return "UnityVector{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", w=" + w +
                '}';
    }
}

package com.btxtech.unityconverter.unity.model;

import com.btxtech.shared.datatypes.Vertex;

public class UnityVector {
    /**
     * https://www.javatips.net/api/robotutils-master/src/main/java/robotutils/Quaternion.java
     *
     * This defines the north pole singularity cutoff when converting
     * from quaternions to Euler angles.
     */
    public static final double SINGULARITY_NORTH_POLE = 0.49999;

    /**
     * https://www.javatips.net/api/robotutils-master/src/main/java/robotutils/Quaternion.java
     *
     * This defines the south pole singularity cutoff when converting
     * from quaternions to Euler angles.
     */
    public static final double SINGULARITY_SOUTH_POLE = -0.49999;

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

    /**
     * https://www.javatips.net/api/robotutils-master/src/main/java/robotutils/Quaternion.java
     * <p>
     * Returns the roll component of the quaternion if it is represented
     * as standard roll-pitch-yaw Euler angles.
     *
     * @return the roll (x-axis rotation) of the robot.
     */
    public double toRoll() {
        // This is a test for singularities
        double test = x*y + z*w;

        // Special case for north pole
        if (test > SINGULARITY_NORTH_POLE)
            return 0;

        // Special case for south pole
        if (test < SINGULARITY_SOUTH_POLE)
            return 0;

        return Math.atan2(
                2*x*w - 2*y*z,
                1 - 2*x*x - 2*z*z
        );
    }

    /**
     * https://www.javatips.net/api/robotutils-master/src/main/java/robotutils/Quaternion.java
     *
     * Returns the pitch component of the quaternion if it is represented
     * as standard roll-pitch-yaw Euler angles.
     * @return the pitch (y-axis rotation) of the robot.
     */
    public double toPitch() {
        // This is a test for singularities
        double test = x*y + z*w;

        // Special case for north pole
        if (test > SINGULARITY_NORTH_POLE)
            return Math.PI/2;

        // Special case for south pole
        if (test < SINGULARITY_SOUTH_POLE)
            return -Math.PI/2;

        return Math.asin(2*test);
    }

    /**
     * https://www.javatips.net/api/robotutils-master/src/main/java/robotutils/Quaternion.java
     *
     * Returns the yaw component of the quaternion if it is represented
     * as standard roll-pitch-yaw Euler angles.
     * @return the yaw (z-axis rotation) of the robot.
     */
    public double toYaw() {
        // This is a test for singularities
        double test = x*y + z*w;

        // Special case for north pole
        if (test > SINGULARITY_NORTH_POLE)
            return 2 * Math.atan2(x, w);

        // Special case for south pole
        if (test < SINGULARITY_SOUTH_POLE)
            return -2 * Math.atan2(x, w);

        return Math.atan2(
                2*y*w - 2*x*z,
                1 - 2*y*y - 2*z*z
        );

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

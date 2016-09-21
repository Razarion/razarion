package com.btxtech.shared.gameengine.datatypes.itemtype;

/**
 * Created by Beat
 * 16.09.2016.
 */
public class PhysicalAreaConfig {
    private double radius;
    private Double angularVelocity; //Grad per second
    private Double speed;
    private Double acceleration;
    private Double minTurnSpeed;

    public double getRadius() {
        return radius;
    }

    public PhysicalAreaConfig setRadius(double radius) {
        this.radius = radius;
        return this;
    }

    public Double getAngularVelocity() {
        return angularVelocity;
    }

    public PhysicalAreaConfig setAngularVelocity(Double angularVelocity) {
        this.angularVelocity = angularVelocity;
        return this;
    }

    public Double getSpeed() {
        return speed;
    }

    public PhysicalAreaConfig setSpeed(Double speed) {
        this.speed = speed;
        return this;
    }

    public Double getAcceleration() {
        return acceleration;
    }

    public PhysicalAreaConfig setAcceleration(Double acceleration) {
        this.acceleration = acceleration;
        return this;
    }

    public Double getMinTurnSpeed() {
        return minTurnSpeed;
    }

    public PhysicalAreaConfig setMinTurnSpeed(Double minTurnSpeed) {
        this.minTurnSpeed = minTurnSpeed;
        return this;
    }

    public boolean fulfilledDirectional() {
        return angularVelocity != null;
    }

    public boolean fulfilledMovable() {
        return fulfilledDirectional() && speed != null && acceleration != null && minTurnSpeed != null;
    }

}

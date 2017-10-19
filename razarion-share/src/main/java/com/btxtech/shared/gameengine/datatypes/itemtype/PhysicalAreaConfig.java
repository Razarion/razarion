package com.btxtech.shared.gameengine.datatypes.itemtype;

import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

/**
 * Created by Beat
 * 16.09.2016.
 */
public class PhysicalAreaConfig {
    private double radius;
    private boolean fixVerticalNorm;
    private TerrainType terrainType;
    private Double angularVelocity; //Rad per second
    private Double speed;
    private Double acceleration;


    public double getRadius() {
        return radius;
    }

    public PhysicalAreaConfig setRadius(double radius) {
        this.radius = radius;
        return this;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public PhysicalAreaConfig setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
        return this;
    }

    // Errai can not handle is-getter. Most likely chained properties with UI binding.
    public boolean getFixVerticalNorm() {
        return fixVerticalNorm;
    }

    public PhysicalAreaConfig setFixVerticalNorm(boolean fixVerticalNorm) {
        this.fixVerticalNorm = fixVerticalNorm;
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

    public boolean fulfilledMovable() {
        return angularVelocity != null && speed != null && acceleration != null;
    }

}

package com.btxtech.shared.gameengine.datatypes.itemtype;

import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;
import org.teavm.flavour.json.JsonPersistable;

/**
 * Created by Beat
 * 16.09.2016.
 */
@JsType
@JsonPersistable
public class PhysicalAreaConfig {
    private double radius;
    private boolean fixVerticalNorm;
    private TerrainType terrainType;
    private Double angularVelocity; //Rad per second
    private Double speed;
    private Double acceleration;
    private Double startAngleSlowDown;
    private Double endAngleSlowDown;

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean isFixVerticalNorm() {
        return fixVerticalNorm;
    }

    public void setFixVerticalNorm(boolean fixVerticalNorm) {
        this.fixVerticalNorm = fixVerticalNorm;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }

    public @Nullable Double getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(@Nullable Double angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public @Nullable Double getSpeed() {
        return speed;
    }

    public void setSpeed(@Nullable Double speed) {
        this.speed = speed;
    }

    public @Nullable Double getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(@Nullable Double acceleration) {
        this.acceleration = acceleration;
    }

    public @Nullable Double getStartAngleSlowDown() {
        return startAngleSlowDown;
    }

    public void setStartAngleSlowDown(@Nullable Double startAngleSlowDown) {
        this.startAngleSlowDown = startAngleSlowDown;
    }

    public @Nullable Double getEndAngleSlowDown() {
        return endAngleSlowDown;
    }

    public void setEndAngleSlowDown(@Nullable Double endAngleSlowDown) {
        this.endAngleSlowDown = endAngleSlowDown;
    }

    public PhysicalAreaConfig radius(double radius) {
        setRadius(radius);
        return this;
    }

    public PhysicalAreaConfig fixVerticalNorm(boolean fixVerticalNorm) {
        setFixVerticalNorm(fixVerticalNorm);
        return this;
    }

    public PhysicalAreaConfig terrainType(TerrainType terrainType) {
        setTerrainType(terrainType);
        return this;
    }

    public PhysicalAreaConfig angularVelocity(Double angularVelocity) {
        setAngularVelocity(angularVelocity);
        return this;
    }

    public PhysicalAreaConfig speed(Double speed) {
        setSpeed(speed);
        return this;
    }

    public PhysicalAreaConfig acceleration(Double acceleration) {
        setAcceleration(acceleration);
        return this;
    }

    public PhysicalAreaConfig startAngleSlowDown(Double startAngleSlowDown) {
        setStartAngleSlowDown(startAngleSlowDown);
        return this;
    }

    public PhysicalAreaConfig endAngleSlowDown(Double endAngleSlowDown) {
        setEndAngleSlowDown(endAngleSlowDown);
        return this;
    }


    @SuppressWarnings("unused") // Used by Angular
    public boolean fulfilledMovable() {
        return angularVelocity != null && speed != null && acceleration != null;
    }
}

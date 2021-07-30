package com.btxtech.shared.dto;

/**
 * Created by Beat
 * on 07.07.2017.
 */
public class DrivewayConfig implements Config {
    private int id;
    private String internalName;
    double angle; // 0 is flat, 90 is perpendicularly

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public DrivewayConfig id(int id) {
        this.id = id;
        return this;
    }

    public DrivewayConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public DrivewayConfig angle(double angle) {
        setAngle(angle);
        return this;
    }

    public double calculateDrivewayLength(double height) {
        return Math.abs(height) / Math.tan(angle);
    }
}

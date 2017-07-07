package com.btxtech.shared.dto;

/**
 * Created by Beat
 * on 07.07.2017.
 */
public class DrivewayConfig {
    private int id;
    double angle; // 0 is flat, 90 is perpendicularly

    public int getId() {
        return id;
    }

    public DrivewayConfig setId(int id) {
        this.id = id;
        return this;
    }

    public double getAngle() {
        return angle;
    }

    public DrivewayConfig setAngle(double angle) {
        this.angle = angle;
        return this;
    }

    public double calculateDrivewayLength(double height) {
        return height / Math.tan(angle);
    }
}

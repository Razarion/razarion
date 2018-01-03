package com.btxtech.server.persistence.tracker;

import java.util.Date;

/**
 * Created by Beat
 * on 03.01.2018.
 */
public class PerfmonTerrainTileDetail {
    private Date clientStartTime;
    private double positionX;
    private double positionY;
    private int duration;

    public Date getClientStartTime() {
        return clientStartTime;
    }

    public PerfmonTerrainTileDetail setClientStartTime(Date clientStartTime) {
        this.clientStartTime = clientStartTime;
        return this;
    }

    public double getXPos() {
        return positionX;
    }

    public PerfmonTerrainTileDetail setXPos(double xPos) {
        this.positionX = xPos;
        return this;
    }

    public double getPositionX() {
        return positionX;
    }

    public PerfmonTerrainTileDetail setPositionX(double positionX) {
        this.positionX = positionX;
        return this;
    }

    public double getPositionY() {
        return positionY;
    }

    public PerfmonTerrainTileDetail setPositionY(double positionY) {
        this.positionY = positionY;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public PerfmonTerrainTileDetail setDuration(int duration) {
        this.duration = duration;
        return this;
    }
}

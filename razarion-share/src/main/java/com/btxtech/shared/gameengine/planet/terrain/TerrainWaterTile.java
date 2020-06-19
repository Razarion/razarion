package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Float32ArrayEmu;

/**
 * Created by Beat
 * 10.04.2017.
 */
public class TerrainWaterTile {
    private int slopeConfigId;
    private Float32ArrayEmu positions;
    private Float32ArrayEmu shallowPositions;
    private Float32ArrayEmu shallowUvs;
    private boolean positionsSet; // positions == null not working -> Float32ArrayEmu to Float32Array problem
    private boolean shallowPositionsSet; // shallowPositions == null not working -> Float32ArrayEmu to Float32Array problem

    public int getSlopeConfigId() {
        return slopeConfigId;
    }

    public void setSlopeConfigId(int slopeConfigId) {
        this.slopeConfigId = slopeConfigId;
    }

    public void setPositions(Float32ArrayEmu positions) {
        positionsSet = true;
        this.positions = positions;
    }

    public Float32ArrayEmu getPositions() {
        return positions;
    }

    public Float32ArrayEmu getShallowPositions() {
        return shallowPositions;
    }

    public void setShallowPositions(Float32ArrayEmu shallowPositions) {
        shallowPositionsSet = true;
        this.shallowPositions = shallowPositions;
    }

    public Float32ArrayEmu getShallowUvs() {
        return shallowUvs;
    }

    public void setShallowUvs(Float32ArrayEmu shallowUvs) {
        this.shallowUvs = shallowUvs;
    }

    public boolean isPositionsSet() {
        return positionsSet;
    }

    public boolean isShallowPositionsSet() {
        return shallowPositionsSet;
    }
}
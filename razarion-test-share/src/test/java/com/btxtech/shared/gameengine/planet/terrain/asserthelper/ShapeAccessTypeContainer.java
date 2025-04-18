package com.btxtech.shared.gameengine.planet.terrain.asserthelper;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

/**
 * Created by Beat
 * on 10.11.2017.
 */
public class ShapeAccessTypeContainer {
    private double height;
    private TerrainType terrainType;
    private Vertex norm;

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }

    public Vertex getNorm() {
        return norm;
    }

    public void setNorm(Vertex norm) {
        this.norm = norm;
    }

    public ShapeAccessTypeContainer height(double height) {
        setHeight(height);
        return this;
    }

    public ShapeAccessTypeContainer terrainType(TerrainType terrainType) {
        setTerrainType(terrainType);
        return this;
    }

    public ShapeAccessTypeContainer norm(Vertex norm) {
        setNorm(norm);
        return this;
    }
}

package com.btxtech.shared.gameengine.planet.terrain.asserthelper;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import org.junit.Assert;

/**
 * Created by Beat
 * on 10.11.2017.
 */
public class ShapeAccessTypeContainer {
    private DecimalPosition samplePosition;
    private double height;
    private TerrainType terrainType;
    private Vertex norm;

    public DecimalPosition getSamplePosition() {
        return samplePosition;
    }

    public void setSamplePosition(DecimalPosition samplePosition) {
        this.samplePosition = samplePosition;
    }

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

    public void assertHeight(double height) {
        Assert.assertEquals("Height", height, this.height, 0.001);
    }

    public void assertNorm(Vertex norm) {
        Assert.assertTrue("Norm", this.norm.equalsDelta(norm, 0.001));
    }

    public void assertTerrainType(TerrainType terrainType) {
        if ((terrainType == null || terrainType == TerrainType.LAND) && (this.terrainType == null || this.terrainType == TerrainType.LAND)) {
            return;
        }
        Assert.assertEquals("TerrainType. At: " + samplePosition, terrainType, this.terrainType);
    }
}

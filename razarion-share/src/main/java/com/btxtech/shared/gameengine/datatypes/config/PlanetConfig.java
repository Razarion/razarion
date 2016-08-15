package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Portable
public class PlanetConfig {
    private Rectangle dimension;
    private List<TerrainSlopePosition> terrainSlopePositions;
    private List<TerrainObjectPosition> terrainObjectPositions;
    private Map<Integer, Integer> itemTypeLimitation;
    private double waterLevel;
    private double shape3DGeneralScale;

    public List<TerrainSlopePosition> getTerrainSlopePositions() {
        return terrainSlopePositions;
    }

    public PlanetConfig setTerrainSlopePositions(List<TerrainSlopePosition> terrainSlopePositions) {
        this.terrainSlopePositions = terrainSlopePositions;
        return this;
    }

    public List<TerrainObjectPosition> getTerrainObjectPositions() {
        return terrainObjectPositions;
    }

    public PlanetConfig setTerrainObjectPositions(List<TerrainObjectPosition> terrainObjectPositions) {
        this.terrainObjectPositions = terrainObjectPositions;
        return this;
    }

    public PlanetConfig setItemTypeLimitation(Map<Integer, Integer> itemTypeLimitation) {
        this.itemTypeLimitation = itemTypeLimitation;
        return this;
    }

    public int getLimitation4ItemType(int itemTypeId) {
        Integer limitation = itemTypeLimitation.get(itemTypeId);
        if (limitation != null) {
            return limitation;
        } else {
            return 0;
        }
    }

    public double getWaterLevel() {
        return waterLevel;
    }

    public PlanetConfig setWaterLevel(double waterLevel) {
        this.waterLevel = waterLevel;
        return this;
    }

    public double getShape3DGeneralScale() {
        return shape3DGeneralScale;
    }

    public PlanetConfig setShape3DGeneralScale(double shape3DGeneralScale) {
        this.shape3DGeneralScale = shape3DGeneralScale;
        return this;
    }

}

package com.btxtech.shared.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 11.09.2017.
 */
public class TerrainEditorLoad {
    private List<TerrainSlopePosition> slopes;
    private List<TerrainObjectPosition> terrainObjects = new ArrayList<>();

    public List<TerrainSlopePosition> getSlopes() {
        return slopes;
    }

    public void setSlopes(List<TerrainSlopePosition> slopes) {
        this.slopes = slopes;
    }

    public List<TerrainObjectPosition> getTerrainObjects() {
        return terrainObjects;
    }

    public void setTerrainObjects(List<TerrainObjectPosition> terrainObjects) {
        this.terrainObjects = terrainObjects;
    }
}

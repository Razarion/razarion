package com.btxtech.shared.dto;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Portable
public class PlanetConfig {
    private GroundSkeleton groundSkeleton;
    private List<SlopeSkeleton> slopeSkeletons;
    private List<TerrainSlopePosition> terrainSlopePositions;
    private List<TerrainObject> terrainObjects;
    private List<TerrainObjectPosition> terrainObjectPositions;

    public GroundSkeleton getGroundSkeleton() {
        return groundSkeleton;
    }

    public void setGroundSkeleton(GroundSkeleton groundSkeleton) {
        this.groundSkeleton = groundSkeleton;
    }

    public List<SlopeSkeleton> getSlopeSkeletons() {
        return slopeSkeletons;
    }

    public void setSlopeSkeletons(List<SlopeSkeleton> slopeSkeletons) {
        this.slopeSkeletons = slopeSkeletons;
    }

    public List<TerrainSlopePosition> getTerrainSlopePositions() {
        return terrainSlopePositions;
    }

    public void setTerrainSlopePositions(List<TerrainSlopePosition> terrainSlopePositions) {
        this.terrainSlopePositions = terrainSlopePositions;
    }

    public List<TerrainObject> getTerrainObjects() {
        return terrainObjects;
    }

    public void setTerrainObjects(List<TerrainObject> terrainObjects) {
        this.terrainObjects = terrainObjects;
    }

    public List<TerrainObjectPosition> getTerrainObjectPositions() {
        return terrainObjectPositions;
    }

    public void setTerrainObjectPositions(List<TerrainObjectPosition> terrainObjectPositions) {
        this.terrainObjectPositions = terrainObjectPositions;
    }
}

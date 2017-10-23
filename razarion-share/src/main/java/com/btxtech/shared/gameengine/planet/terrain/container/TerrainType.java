package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.utils.CollectionUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Beat
 * on 03.10.2017.
 */
public enum TerrainType {
    LAND(true),
    WATER(true),
    LAND_COAST(false),
    WATER_COAST(false),
    BLOCKED(false);

    private static final Set<TerrainType> LAND_WATER_COAST = new HashSet<>(Arrays.asList(LAND_COAST, WATER_COAST));
    private boolean areaCheck;

    TerrainType(boolean areaCheck) {
        this.areaCheck = areaCheck;
    }

    public boolean isAreaCheck() {
        return areaCheck;
    }

    public static boolean isAllowed(TerrainType expected, TerrainType actual) {
        return actual == null && expected == LAND || expected == actual;
    }

    public static boolean isAtLeaseOneAllowedOrdinal(Set<TerrainType> expected, Integer actualOrdinal) {
        if (actualOrdinal == null) {
            return expected.size() == 1 && CollectionUtils.getFirst(expected) == LAND;
        }
        for (TerrainType expectedTerrainType : expected) {
            if (actualOrdinal == expectedTerrainType.ordinal()) {
                return true;
            }
        }
        return false;
    }

    public static Set<TerrainType> getSkippableTerrainType(TerrainType terrainType, TerrainType targetTerrainType) {
        if (terrainType == targetTerrainType) {
            return null;
        }
        if (terrainType == LAND && targetTerrainType == WATER_COAST) {
            return LAND_WATER_COAST;
        }
        throw new IllegalArgumentException("TerrainType.getSkippableTerrainType() terrainType: " + terrainType + " targetTerrainType: " + targetTerrainType);
    }

    public static TerrainType getNullTerrainType() {
        return LAND;
    }
}

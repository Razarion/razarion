package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.utils.CollectionUtils;

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

    private boolean areaCheck;

    TerrainType(boolean areaCheck) {
        this.areaCheck = areaCheck;
    }

    public boolean isAreaCheck() {
        return areaCheck;
    }

    public static boolean isAllowed(TerrainType expected, TerrainType actual) {
        if (actual == null) {
            return expected == getNullTerrainType();
        }
        return expected == actual;
    }

    public static boolean isAtLeaseOneAllowedOrdinal(Set<TerrainType> expected, int actualOrdinal) {
        TerrainType terrainType = TerrainType.fromOrdinal(actualOrdinal);

        if (terrainType == null) {
            return expected.size() == 1 && CollectionUtils.getFirst(expected) == LAND;
        }
        for (TerrainType expectedTerrainType : expected) {
            if (terrainType == expectedTerrainType) {
                return true;
            }
        }
        return false;
    }

    public static TerrainType getNullTerrainType() {
        return LAND;
    }

    /**
     * GWT has problem turn an Integer ordinal to a enum
     * Integer is not working here because Integer.intValue() is not defined (from JSON object)
     *
     * @param terrainTypeOrdinal ordinal or < 0 if none
     * @return TerrainType
     */
    public static TerrainType fromOrdinal(int terrainTypeOrdinal) {
        if (terrainTypeOrdinal < 0) {
            return null;
        }
        return values()[terrainTypeOrdinal];
    }

    /**
     * GWT has problem turn an Integer ordinal to a enum
     * Integer is not working here because Integer.intValue() is not defined (from JSON object)
     *
     * @param terrainType ordinal or < 0 if none
     * @return ordinal or < 0 if terrainType is null
     */
    public static int toOrdinal(TerrainType terrainType) {
        if (terrainType == null) {
            return -1;
        }
        return terrainType.ordinal();
    }
}

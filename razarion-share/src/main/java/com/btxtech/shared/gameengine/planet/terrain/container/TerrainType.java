package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.utils.CollectionUtils;

import java.util.Set;

/**
 * Created by Beat
 * on 03.10.2017.
 */
public enum TerrainType {
    LAND,
    WATER,
    LAND_COAST,
    WATER_COAST,
    BLOCKED;

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
}

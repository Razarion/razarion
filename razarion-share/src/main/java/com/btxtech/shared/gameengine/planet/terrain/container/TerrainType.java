package com.btxtech.shared.gameengine.planet.terrain.container;

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

    public static boolean isAllowedOrdinal(TerrainType expected, Integer actualOrdinal) {
        if (actualOrdinal == null) {
            return expected == LAND;
        }
        return expected.ordinal() == actualOrdinal;
    }
}

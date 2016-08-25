/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.shared.gameengine.datatypes;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: beat
 * Date: 24.04.2010
 * Time: 14:16:30
 */
@Portable
public enum TerrainType {
    LAND(SurfaceType.LAND), // Tank
    WATER(SurfaceType.WATER), // Ships
    COAST(SurfaceType.COAST), // Harbor, land-water-attacking
    LAND_COAST(SurfaceType.LAND, SurfaceType.COAST), // Harbor builder
    WATER_COAST(SurfaceType.WATER, SurfaceType.COAST);  // Water transporter
    final private static Map<SurfaceType, Collection<TerrainType>> surfaceTypeCollectionMap = new HashMap<>();
    private List<SurfaceType> surfaceTypes;

    TerrainType(SurfaceType... surfaceTypes) {
        this.surfaceTypes = Arrays.asList(surfaceTypes);
    }

    public List<SurfaceType> getSurfaceTypes() {
        return surfaceTypes;
    }

    public boolean allowSurfaceType(SurfaceType surfaceType) {
        return surfaceTypes.contains(surfaceType);
    }

    public static Collection<TerrainType> getAllowedTerrainType(SurfaceType surfaceType) {
        Collection<TerrainType> terrainTypes = surfaceTypeCollectionMap.get(surfaceType);
        if (terrainTypes != null) {
            return terrainTypes;
        }

        terrainTypes = new ArrayList<>();
        for (TerrainType terrainType : values()) {
            if (terrainType.getSurfaceTypes().contains(surfaceType)) {
                terrainTypes.add(terrainType);
            }
        }
        surfaceTypeCollectionMap.put(surfaceType, terrainTypes);
        return terrainTypes;
    }

    public static Collection<SurfaceType> leastCommonMultiple(Collection<TerrainType> types) {
        Collection<SurfaceType> lcm = null;
        for (TerrainType type : types) {
            if (lcm == null) {
                lcm = new ArrayList<>(type.getSurfaceTypes());
            } else {
                lcm.retainAll(type.getSurfaceTypes());
            }
        }
        return lcm;
    }

}

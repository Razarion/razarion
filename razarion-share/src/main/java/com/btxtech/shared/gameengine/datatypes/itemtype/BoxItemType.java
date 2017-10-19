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

package com.btxtech.shared.gameengine.datatypes.itemtype;


import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import java.util.List;

public class BoxItemType extends ItemType {
    private Integer ttl;  // seconds
    private double radius;
    private boolean fixVerticalNorm;
    private TerrainType terrainType;
    private List<BoxItemTypePossibility> boxItemTypePossibilities;

    public BoxItemType setRadius(double radius) {
        this.radius = radius;
        return this;
    }

    public double getRadius() {
        return radius;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public BoxItemType setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
        return this;
    }

    public boolean isFixVerticalNorm() {
        return fixVerticalNorm;
    }

    public BoxItemType setFixVerticalNorm(boolean fixVerticalNorm) {
        this.fixVerticalNorm = fixVerticalNorm;
        return this;
    }

    public Integer getTtl() {
        return ttl;
    }

    public List<BoxItemTypePossibility> getBoxItemTypePossibilities() {
        return boxItemTypePossibilities;
    }

    public BoxItemType setTtl(Integer ttl) {
        this.ttl = ttl;
        return this;
    }

    public BoxItemType setBoxItemTypePossibilities(List<BoxItemTypePossibility> boxItemTypePossibilities) {
        this.boxItemTypePossibilities = boxItemTypePossibilities;
        return this;
    }
}

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
import jsinterop.annotations.JsType;
import org.teavm.flavour.json.JsonPersistable;

import java.util.List;

@JsType
@JsonPersistable
public class BoxItemType extends ItemType {
    private Integer ttl;  // seconds
    private double radius;
    private boolean fixVerticalNorm;
    private TerrainType terrainType;
    private List<BoxItemTypePossibility> boxItemTypePossibilities;


    public double getRadius() {
        return radius;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }


    public boolean isFixVerticalNorm() {
        return fixVerticalNorm;
    }

    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setFixVerticalNorm(boolean fixVerticalNorm) {
        this.fixVerticalNorm = fixVerticalNorm;
    }

    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }

    public void setBoxItemTypePossibilities(List<BoxItemTypePossibility> boxItemTypePossibilities) {
        this.boxItemTypePossibilities = boxItemTypePossibilities;
    }

    public List<BoxItemTypePossibility> getBoxItemTypePossibilities() {
        return boxItemTypePossibilities;
    }

    public BoxItemType terrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
        return this;
    }

    public BoxItemType fixVerticalNorm(boolean fixVerticalNorm) {
        this.fixVerticalNorm = fixVerticalNorm;
        return this;
    }

    public BoxItemType radius(double radius) {
        this.radius = radius;
        return this;
    }

    public BoxItemType ttl(Integer ttl) {
        this.ttl = ttl;
        return this;
    }

    public BoxItemType boxItemTypePossibilities(List<BoxItemTypePossibility> boxItemTypePossibilities) {
        this.boxItemTypePossibilities = boxItemTypePossibilities;
        return this;
    }
}

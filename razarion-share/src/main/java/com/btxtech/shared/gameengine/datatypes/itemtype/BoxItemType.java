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


import java.util.List;

public class BoxItemType extends ItemType {
    private int ttl;
    private double radius;
    private List<BoxItemTypePossibility> boxItemTypePossibilities;

    public double getRadius() {
        return radius;
    }

    public int getTtl() {
        return ttl;
    }

    public List<BoxItemTypePossibility> getBoxItemTypePossibilities() {
        return boxItemTypePossibilities;
    }

    public BoxItemType setTtl(int ttl) {
        this.ttl = ttl;
        return this;
    }

    public BoxItemType setBoxItemTypePossibilities(List<BoxItemTypePossibility> boxItemTypePossibilities) {
        this.boxItemTypePossibilities = boxItemTypePossibilities;
        return this;
    }


    public BoxItemType setRadius(double radius) {
        this.radius = radius;
        return this;
    }
}

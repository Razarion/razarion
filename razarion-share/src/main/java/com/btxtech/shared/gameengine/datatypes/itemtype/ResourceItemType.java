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


public class ResourceItemType extends ItemType {
    private double radius;
    private int amount;

    public double getRadius() {
        return radius;
    }

    public ResourceItemType setRadius(double radius) {
        this.radius = radius;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public ResourceItemType setAmount(int amount) {
        this.amount = amount;
        return this;
    }
}

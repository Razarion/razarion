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

/**
 * User: beat
 * Date: 23.12.2009
 * Time: 12:54:48
 */
public class ConsumerType {
    private int wattage;

    /**
     * Used by GWT
     */
    public ConsumerType() {
    }

    public ConsumerType(int wattage) {
        this.wattage = wattage;
    }

    public int getWattage() {
        return wattage;
    }

    public void setWattage(int wattage) {
        this.wattage = wattage;
    }

    public void changeTo(ConsumerType consumerType) {
        wattage = consumerType.wattage;
    }
}

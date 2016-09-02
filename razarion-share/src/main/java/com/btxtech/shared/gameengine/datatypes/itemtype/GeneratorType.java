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
 * Time: 12:44:52
 */
public class GeneratorType {
    private int wattage;

    /**
     * Used by GWT
     */
    public GeneratorType() {
    }

    public GeneratorType(int wattage) {
        this.wattage = wattage;
    }

    public int getWattage() {
        return wattage;
    }

    public void setWattage(int wattage) {
        this.wattage = wattage;
    }

    public void changeTo(GeneratorType generatorType) {
        wattage = generatorType.wattage;
    }
}

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
 * Date: 17.11.2009
 * Time: 23:18:42
 */
public class HouseType {
    private int space;

    /**
     * Used by GWT
     */
    public HouseType() {
    }

    public HouseType(int space) {
        this.space = space;
    }


    public void changeTo(HouseType houseType) {
        space = houseType.space;
    }

    public int getSpace() {
        return space;
    }
}

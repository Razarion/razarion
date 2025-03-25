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

package com.btxtech.shared.datatypes.tracking;

/**
 * User: beat
 * Date: 03.08.2010
 * Time: 22:12:06
 */
public class MouseButtonTracking extends DetailedTracking {
    private int button;
    private boolean down;

    public int getButton() {
        return button;
    }

    public MouseButtonTracking setButton(int button) {
        this.button = button;
        return this;
    }

    public boolean isDown() {
        return down;
    }

    public MouseButtonTracking setDown(boolean down) {
        this.down = down;
        return this;
    }
}

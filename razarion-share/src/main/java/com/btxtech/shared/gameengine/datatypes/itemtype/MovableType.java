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

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * User: beat
 * Date: 17.11.2009
 * Time: 23:12:43
 */
@Portable
public class MovableType {
    private int speed;

    public int getSpeed() {
        return speed;
    }

    public MovableType setSpeed(int speed) {
        this.speed = speed;
        return this;
    }
}

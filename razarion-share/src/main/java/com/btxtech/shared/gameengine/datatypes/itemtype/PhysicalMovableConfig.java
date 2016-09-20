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
 * Time: 23:12:43
 */
public class PhysicalMovableConfig extends PhysicalDirectionConfig {
    private int speed;
    private double acceleration;
    private double minTurnSpeed;

    public int getSpeed() {
        return speed;
    }

    public PhysicalMovableConfig setSpeed(int speed) {
        this.speed = speed;
        return this;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public PhysicalMovableConfig setAcceleration(double acceleration) {
        this.acceleration = acceleration;
        return this;
    }

    public double getMinTurnSpeed() {
        return minTurnSpeed;
    }

    public PhysicalMovableConfig setMinTurnSpeed(double minTurnSpeed) {
        this.minTurnSpeed = minTurnSpeed;
        return this;
    }
}

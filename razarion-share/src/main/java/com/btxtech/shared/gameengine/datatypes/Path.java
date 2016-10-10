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

package com.btxtech.shared.gameengine.datatypes;


import com.btxtech.shared.datatypes.DecimalPosition;


/**
 * User: beat
 * Date: May 27, 2009
 * Time: 6:29:22 PM
 */
public class Path {
    private DecimalPosition destination;
    private double range;

    public Path() {
    }

    @Deprecated
    public Path(DecimalPosition start, DecimalPosition destination, boolean destinationReachable) {
    }

    public DecimalPosition getDestination() {
        return destination;
    }

    public Path setDestination(DecimalPosition destination) {
        this.destination = destination;
        return this;
    }

    public double getRange() {
        return range;
    }

    public Path setRange(double range) {
        this.range = range;
        return this;
    }

    @Deprecated
    public DecimalPosition getAlternativeDestination() {
        return null;
    }

    @Deprecated
    public boolean isDestinationReachable() {
        return false;
    }

    @Deprecated
    public void setDestinationAngel(double destinationAngel) {
    }

}

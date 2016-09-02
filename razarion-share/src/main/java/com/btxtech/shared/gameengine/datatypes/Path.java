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

import java.util.ArrayList;
import java.util.List;


/**
 * User: beat
 * Date: May 27, 2009
 * Time: 6:29:22 PM
 */
public class Path {
    private DecimalPosition start;
    private DecimalPosition destination;
    private DecimalPosition alternativeDestination;
    private boolean destinationReachable;
    private List<DecimalPosition> path;
    private Double destinationAngel;

    /**
     * Used by GWT
     */
    public Path() {
    }

    public Path(DecimalPosition start, DecimalPosition destination, boolean destinationReachable) {
        this.start = start;
        this.destination = destination;
        this.destinationReachable = destinationReachable;
    }

    public void makeSameStartAndDestination() {
        path = new ArrayList<DecimalPosition>();
        path.add(destination);
        destinationAngel = 0.0;
    }

    public DecimalPosition getAlternativeDestination() {
        return alternativeDestination;
    }

    public void setAlternativeDestination(DecimalPosition alternativeDestination) {
        this.alternativeDestination = alternativeDestination;
    }

    public void setPath(List<DecimalPosition> path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalStateException("Path: empty path not allowed");
        }
        if (path.size() == 1) {
            throw new IllegalStateException("Path: more than one entry expected");
        }

        this.path = path;
    }

    public DecimalPosition getStart() {
        return start;
    }

    public DecimalPosition getDestination() {
        return destination;
    }

    public boolean isDestinationReachable() {
        return destinationReachable;
    }

    public List<DecimalPosition> getPath() {
        return path;
    }

    public void setDestinationAngel(double destinationAngel) {
        this.destinationAngel = destinationAngel;
    }

    public DecimalPosition getActualDestination() {
        if (destinationReachable) {
            return destination;
        } else {
            return alternativeDestination;
        }
    }

    public double getActualDestinationAngel() {
        if (destinationAngel != null) {
            return destinationAngel;
        } else if (destinationReachable) {
            DecimalPosition secondLastPoint = path.get(path.size() - 2);
            return secondLastPoint.getAngleToNord(getActualDestination());
        } else {
            return alternativeDestination.getAngleToNord(destination);
        }
    }

    @Override
    public String toString() {
        return "Path{" +
                "start=" + start +
                ", destination=" + destination +
                ", alternativeDestination=" + alternativeDestination +
                ", destinationReachable=" + destinationReachable +
                ", path=" + path +
                ", destinationAngel=" + destinationAngel +
                '}';
    }
}

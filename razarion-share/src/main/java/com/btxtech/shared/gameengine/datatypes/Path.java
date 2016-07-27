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


import com.btxtech.shared.datatypes.Index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * User: beat
 * Date: May 27, 2009
 * Time: 6:29:22 PM
 */
public class Path implements Serializable {
    private Index start;
    private Index destination;
    private Index alternativeDestination;
    private boolean destinationReachable;
    private List<Index> path;
    private Double destinationAngel;

    /**
     * Used by GWT
     */
    protected Path() {
    }

    public Path(Index start, Index destination, boolean destinationReachable) {
        this.start = start;
        this.destination = destination;
        this.destinationReachable = destinationReachable;
    }

    public void makeSameStartAndDestination() {
        path = new ArrayList<Index>();
        path.add(destination);
        destinationAngel = 0.0;
    }

    public Index getAlternativeDestination() {
        return alternativeDestination;
    }

    public void setAlternativeDestination(Index alternativeDestination) {
        this.alternativeDestination = alternativeDestination;
    }

    public void setPath(List<Index> path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalStateException("Path: empty path not allowed");
        }
        if (path.size() == 1) {
            throw new IllegalStateException("Path: more than one entry expected");
        }

        this.path = path;
    }

    public Index getStart() {
        return start;
    }

    public Index getDestination() {
        return destination;
    }

    public boolean isDestinationReachable() {
        return destinationReachable;
    }

    public List<Index> getPath() {
        return path;
    }

    public void setDestinationAngel(double destinationAngel) {
        this.destinationAngel = destinationAngel;
    }

    public Index getActualDestination() {
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
            Index secondLastPoint = path.get(path.size() - 2);
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
                ", path=" + Index.toString(path) +
                ", destinationAngel=" + destinationAngel +
                '}';
    }
}

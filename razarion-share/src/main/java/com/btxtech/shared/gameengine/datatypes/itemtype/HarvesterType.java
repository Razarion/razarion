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
 * Time: 23:23:38
 */
public class HarvesterType {
    private int range;
    private double progress;

    public int getRange() {
        return range;
    }

    public HarvesterType setRange(int range) {
        this.range = range;
        return this;
    }

    public double getProgress() {
        return progress;
    }

    public HarvesterType setProgress(double progress) {
        this.progress = progress;
        return this;
    }
}

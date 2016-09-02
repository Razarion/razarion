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
    private ItemClipPosition harvesterClip;

    /**
     * Used by GWT
     */
    public HarvesterType() {
    }

    public HarvesterType(int range, double progress, ItemClipPosition harvesterClip) {
        this.range = range;
        this.progress = progress;
        this.harvesterClip = harvesterClip;
    }

    public int getRange() {
        return range;
    }

    public double getProgress() {
        return progress;
    }

    public ItemClipPosition getHarvesterClip() {
        return harvesterClip;
    }

    public void changeTo(HarvesterType harvesterType) {
        range = harvesterType.range;
        progress = harvesterType.progress;
        harvesterClip = harvesterType.harvesterClip;
    }
}

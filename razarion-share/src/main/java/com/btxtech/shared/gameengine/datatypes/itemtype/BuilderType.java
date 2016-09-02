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

import java.util.Collection;

/**
 * User: beat
 * Date: 21.11.2009
 * Time: 23:53:27
 */
public class BuilderType {
    private int range;
    private double progress;
    private Collection<Integer> ableToBuild;
    private ItemClipPosition buildupClip;

    /**
     * Used by GWT
     */
    public BuilderType() {
    }

    public BuilderType(int range, double progress, Collection<Integer> ableToBuild, ItemClipPosition buildupClip) {
        this.range = range;
        this.progress = progress;
        this.ableToBuild = ableToBuild;
        this.buildupClip = buildupClip;
    }

    public int getRange() {
        return range;
    }

    public double getProgress() {
        return progress;
    }

    public boolean isAbleToBuild(int itemTypeId) {
        return ableToBuild.contains(itemTypeId);
    }

    public Collection<Integer> getAbleToBuild() {
        return ableToBuild;
    }

    public ItemClipPosition getBuildupClip() {
        return buildupClip;
    }

    public void changeTo(BuilderType builderType) {
        range = builderType.range;
        progress = builderType.progress;
        ableToBuild = builderType.ableToBuild;
        buildupClip = builderType.buildupClip;
    }
}

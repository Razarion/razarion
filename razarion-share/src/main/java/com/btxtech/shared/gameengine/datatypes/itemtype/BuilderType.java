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

import com.btxtech.shared.datatypes.Vertex;

import java.util.List;

/**
 * User: beat
 * Date: 21.11.2009
 * Time: 23:53:27
 */
public class BuilderType {
    private double range;
    private double progress;
    private List<Integer> ableToBuildIds;
    private Vertex animationOrigin;
    private Integer animationShape3dId;

    public double getRange() {
        return range;
    }

    public BuilderType setRange(double range) {
        this.range = range;
        return this;
    }

    public double getProgress() {
        return progress;
    }

    public BuilderType setProgress(double progress) {
        this.progress = progress;
        return this;
    }

    public List<Integer> getAbleToBuildIds() {
        return ableToBuildIds;
    }

    public BuilderType setAbleToBuildIds(List<Integer> ableToBuildIds) {
        this.ableToBuildIds = ableToBuildIds;
        return this;
    }

    public boolean checkAbleToBuild(int itemTypeId) {
        return ableToBuildIds.contains(itemTypeId);
    }

    public Vertex getAnimationOrigin() {
        return animationOrigin;
    }

    public BuilderType setAnimationOrigin(Vertex animationOrigin) {
        this.animationOrigin = animationOrigin;
        return this;
    }

    public Integer getAnimationShape3dId() {
        return animationShape3dId;
    }

    public BuilderType setAnimationShape3dId(Integer animationShape3dId) {
        this.animationShape3dId = animationShape3dId;
        return this;
    }
}

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

import org.teavm.flavour.json.JsonPersistable;

import java.util.List;

/**
 * User: beat
 * Date: 17.11.2009
 * Time: 23:18:42
 */
@JsonPersistable
public class FactoryType {
    private double progress;
    /**
     * Seconds the factory waits BEFORE starting to advance build progress.
     * Used to give the client time to play the "intro" animation (e.g. grid/platform
     * going down) before the progress-driven animation begins. 0 = no warmup.
     */
    private double animationIntroSeconds;
    private double animationOutroSeconds;
    /**
     * Rally point offset relative to the factory center. The produced unit spawns at
     * factoryPosition + (rallyOffsetX, rallyOffsetY). If both are 0 the rally point is
     * computed automatically. "Y" here is the game-world Z (DecimalPosition convention).
     */
    private double rallyOffsetX;
    private double rallyOffsetY;
    private List<Integer> ableToBuildIds;

    public double getProgress() {
        return progress;
    }

    public FactoryType setProgress(double progress) {
        this.progress = progress;
        return this;
    }

    public List<Integer> getAbleToBuildIds() {
        return ableToBuildIds;
    }

    public FactoryType setAbleToBuildIds(List<Integer> ableToBuildIds) {
        this.ableToBuildIds = ableToBuildIds;
        return this;
    }

    public double getAnimationIntroSeconds() {
        return animationIntroSeconds;
    }

    public FactoryType setAnimationIntroSeconds(double animationIntroSeconds) {
        this.animationIntroSeconds = animationIntroSeconds;
        return this;
    }

    public double getAnimationOutroSeconds() {
        return animationOutroSeconds;
    }

    public FactoryType setAnimationOutroSeconds(double animationOutroSeconds) {
        this.animationOutroSeconds = animationOutroSeconds;
        return this;
    }

    public double getRallyOffsetX() {
        return rallyOffsetX;
    }

    public FactoryType setRallyOffsetX(double rallyOffsetX) {
        this.rallyOffsetX = rallyOffsetX;
        return this;
    }

    public double getRallyOffsetY() {
        return rallyOffsetY;
    }

    public FactoryType setRallyOffsetY(double rallyOffsetY) {
        this.rallyOffsetY = rallyOffsetY;
        return this;
    }

    public boolean isAbleToBuild(int itemTypeId) {
        return ableToBuildIds.contains(itemTypeId);
    }

}

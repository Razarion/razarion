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

import jsinterop.annotations.JsType;
import org.teavm.flavour.json.JsonPersistable;

import java.util.List;

/**
 * User: beat
 * Date: 21.11.2009
 * Time: 23:53:27
 */
@JsType
@JsonPersistable
public class BuilderType {
    private double range;
    private double rangeOtherTerrain;
    private double progress;
    /**
     * Seconds the builder waits at the build position BEFORE starting to add buildup to the target.
     * Used to give the client time to play a "build intro" animation (e.g. arm extending) before
     * the target item is created and the build beam starts. 0 = no warmup (legacy behavior).
     */
    private double buildAnimationWarmupSeconds;
    /**
     * Seconds the builder stays at the build position AFTER the target reaches buildup 1.0,
     * before releasing the build job. Used to give the client time to play a "build outro"
     * animation (e.g. arm retracting). 0 = no cooldown (legacy behavior).
     */
    private double buildAnimationCooldownSeconds;
    private List<Integer> ableToBuildIds;

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public double getRangeOtherTerrain() {
        return rangeOtherTerrain;
    }

    public void setRangeOtherTerrain(double rangeOtherTerrain) {
        this.rangeOtherTerrain = rangeOtherTerrain;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public double getBuildAnimationWarmupSeconds() {
        return buildAnimationWarmupSeconds;
    }

    public void setBuildAnimationWarmupSeconds(double buildAnimationWarmupSeconds) {
        this.buildAnimationWarmupSeconds = buildAnimationWarmupSeconds;
    }

    public double getBuildAnimationCooldownSeconds() {
        return buildAnimationCooldownSeconds;
    }

    public void setBuildAnimationCooldownSeconds(double buildAnimationCooldownSeconds) {
        this.buildAnimationCooldownSeconds = buildAnimationCooldownSeconds;
    }

    public List<Integer> getAbleToBuildIds() {
        return ableToBuildIds;
    }

    public void setAbleToBuildIds(List<Integer> ableToBuildIds) {
        this.ableToBuildIds = ableToBuildIds;
    }

    public BuilderType range(double range) {
        setRange(range);
        return this;
    }

    public BuilderType rangeOtherTerrain(double rangeOtherTerrain) {
        setRangeOtherTerrain(rangeOtherTerrain);
        return this;
    }

    public BuilderType progress(double progress) {
        setProgress(progress);
        return this;
    }

    public BuilderType buildAnimationWarmupSeconds(double buildAnimationWarmupSeconds) {
        setBuildAnimationWarmupSeconds(buildAnimationWarmupSeconds);
        return this;
    }

    public BuilderType buildAnimationCooldownSeconds(double buildAnimationCooldownSeconds) {
        setBuildAnimationCooldownSeconds(buildAnimationCooldownSeconds);
        return this;
    }

    public BuilderType ableToBuildIds(List<Integer> ableToBuildIds) {
        setAbleToBuildIds(ableToBuildIds);
        return this;
    }

    public boolean checkAbleToBuild(int itemTypeId) {
        return ableToBuildIds.contains(itemTypeId);
    }

}

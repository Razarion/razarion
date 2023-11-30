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

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;

import java.util.List;

/**
 * User: beat
 * Date: 21.11.2009
 * Time: 23:53:27
 */
@JsType
public class BuilderType {
    private double range;
    private double progress;
    private List<Integer> ableToBuildIds;
    @CollectionReference(CollectionReferenceType.PARTICLE_SYSTEM)
    private Integer particleSystemConfigId;

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public List<Integer> getAbleToBuildIds() {
        return ableToBuildIds;
    }

    public void setAbleToBuildIds(List<Integer> ableToBuildIds) {
        this.ableToBuildIds = ableToBuildIds;
    }

    public @Nullable Integer getParticleSystemConfigId() {
        return particleSystemConfigId;
    }

    public void setParticleSystemConfigId(@Nullable Integer particleSystemConfigId) {
        this.particleSystemConfigId = particleSystemConfigId;
    }

    public BuilderType range(double range) {
        setRange(range);
        return this;
    }

    public BuilderType progress(double progress) {
        setProgress(progress);
        return this;
    }

    public BuilderType ableToBuildIds(List<Integer> ableToBuildIds) {
        setAbleToBuildIds(ableToBuildIds);
        return this;
    }

    public boolean checkAbleToBuild(int itemTypeId) {
        return ableToBuildIds.contains(itemTypeId);
    }

    public BuilderType particleSystemConfigId(Integer particleSystemConfigId) {
        setParticleSystemConfigId(particleSystemConfigId);
        return this;
    }
}

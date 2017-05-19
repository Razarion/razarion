package com.btxtech.shared.gameengine.datatypes.itemtype;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 20.12.2016.
 */
public class DemolitionParticleConfig {
    private Integer particleConfigId;
    private Vertex position;

    public Integer getParticleConfigId() {
        return particleConfigId;
    }

    public DemolitionParticleConfig setParticleConfigId(Integer particleConfigId) {
        this.particleConfigId = particleConfigId;
        return this;
    }

    public Vertex getPosition() {
        return position;
    }

    public DemolitionParticleConfig setPosition(Vertex position) {
        this.position = position;
        return this;
    }
}

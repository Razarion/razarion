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

    public void setParticleConfigId(Integer particleConfigId) {
        this.particleConfigId = particleConfigId;
    }

    public Vertex getPosition() {
        return position;
    }

    public void setPosition(Vertex position) {
        this.position = position;
    }

    public DemolitionParticleConfig particleConfigId(Integer particleConfigId) {
        setParticleConfigId(particleConfigId);
        return this;
    }

    public DemolitionParticleConfig position(Vertex position) {
        setPosition(position);
        return this;
    }
}

package com.btxtech.shared.gameengine.datatypes.itemtype;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 20.12.2016.
 */
public class DemolitionParticleConfig {
    private int particleEmitterSequenceConfigId;
    private Vertex position;

    public int getParticleEmitterSequenceConfigId() {
        return particleEmitterSequenceConfigId;
    }

    public DemolitionParticleConfig setParticleEmitterSequenceConfigId(int particleEmitterSequenceConfigId) {
        this.particleEmitterSequenceConfigId = particleEmitterSequenceConfigId;
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

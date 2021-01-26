package com.btxtech.uiservice.effects;

import com.btxtech.uiservice.particle.ParticleEmitterSequenceHandler;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 10.02.2017.
 */
public class DemolitionBaseItemEntry {
    private int demolitionStep;
    private Collection<ParticleEmitterSequenceHandler> particleEmitterSequenceHandlers = new ArrayList<>();

    public DemolitionBaseItemEntry() {
        demolitionStep = -1;
    }

    public int getDemolitionStep() {
        return demolitionStep;
    }

    public void setDemolitionStep(int demolitionStep) {
        this.demolitionStep = demolitionStep;
    }

    public void disposeParticles() {
        for (ParticleEmitterSequenceHandler particleEmitterSequenceHandler : particleEmitterSequenceHandlers) {
            particleEmitterSequenceHandler.dispose();
        }
        particleEmitterSequenceHandlers.clear();
    }

    public void addParticleHandler(ParticleEmitterSequenceHandler particleEmitterSequenceHandler) {
        particleEmitterSequenceHandlers.add(particleEmitterSequenceHandler);
    }
}

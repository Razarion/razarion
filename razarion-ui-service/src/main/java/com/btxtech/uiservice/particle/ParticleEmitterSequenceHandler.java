package com.btxtech.uiservice.particle;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 10.02.2017.
 */
public class ParticleEmitterSequenceHandler {
    private Collection<DependentParticleEmitter> dependentParticleEmitters = new ArrayList<>();
    private ParticleService particleService;

    public ParticleEmitterSequenceHandler(ParticleService particleService) {
        this.particleService = particleService;
    }

    public void addDependentParticleEmitter(DependentParticleEmitter dependentParticleEmitter) {
        dependentParticleEmitters.add(dependentParticleEmitter);
    }

    public void dispose() {
        for (DependentParticleEmitter dependentParticleEmitter : dependentParticleEmitters) {
            particleService.removeParticleEmitter(dependentParticleEmitter);
        }
    }
}

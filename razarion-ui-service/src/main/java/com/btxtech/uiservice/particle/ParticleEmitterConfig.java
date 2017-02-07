package com.btxtech.uiservice.particle;

/**
 * Created by Beat
 * 05.02.2017.
 */
public class ParticleEmitterConfig {
    private ParticleConfig particleConfig;
    private int emittingDelay;
    private int emittingCount;
    private double generationRandomDistance;

    public ParticleConfig getParticleConfig() {
        return particleConfig;
    }

    public ParticleEmitterConfig setParticleConfig(ParticleConfig particleConfig) {
        this.particleConfig = particleConfig;
        return this;
    }

    public int getEmittingDelay() {
        return emittingDelay;
    }

    public ParticleEmitterConfig setEmittingDelay(int emittingDelay) {
        this.emittingDelay = emittingDelay;
        return this;
    }

    public int getEmittingCount() {
        return emittingCount;
    }

    public ParticleEmitterConfig setEmittingCount(int emittingCount) {
        this.emittingCount = emittingCount;
        return this;
    }

    public double getGenerationRandomDistance() {
        return generationRandomDistance;
    }

    public ParticleEmitterConfig setGenerationRandomDistance(double generationRandomDistance) {
        this.generationRandomDistance = generationRandomDistance;
        return this;
    }
}

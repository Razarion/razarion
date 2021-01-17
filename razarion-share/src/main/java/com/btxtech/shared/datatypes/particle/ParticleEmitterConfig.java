package com.btxtech.shared.datatypes.particle;

/**
 * Created by Beat
 * 05.02.2017.
 */
public class ParticleEmitterConfig<T extends ParticleEmitterConfig<?>> {
    private int id;
    private String internalName;
    private ParticleConfig particleConfig;
    private int emittingDelay;
    private int emittingCount;
    private double generationRandomDistance;

    public int getId() {
        return id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public ParticleConfig getParticleConfig() {
        return particleConfig;
    }

    public void setParticleConfig(ParticleConfig particleConfig) {
        this.particleConfig = particleConfig;
    }

    public int getEmittingDelay() {
        return emittingDelay;
    }

    public void setEmittingDelay(int emittingDelay) {
        this.emittingDelay = emittingDelay;
    }

    public int getEmittingCount() {
        return emittingCount;
    }

    public void setEmittingCount(int emittingCount) {
        this.emittingCount = emittingCount;
    }

    public double getGenerationRandomDistance() {
        return generationRandomDistance;
    }

    public void setGenerationRandomDistance(double generationRandomDistance) {
        this.generationRandomDistance = generationRandomDistance;
    }

    public T id(int id) {
        this.id = id;
        return (T) this;
    }

    public T internalName(String internalName) {
        setInternalName(internalName);
        return (T) this;
    }

    public T particleConfig(ParticleConfig particleConfig) {
        setParticleConfig(particleConfig);
        return (T) this;
    }

    public T emittingDelay(int emittingDelay) {
        setEmittingDelay(emittingDelay);
        return (T) this;
    }

    public T emittingCount(int emittingCount) {
        setEmittingCount(emittingCount);
        return (T) this;
    }

    public T generationRandomDistance(double generationRandomDistance) {
        setGenerationRandomDistance(generationRandomDistance);
        return (T) this;
    }
}

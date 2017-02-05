package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 05.02.2017.
 */
public class ParticleEmitterConfig {
    private int start;
    private int ttl;
    private Vertex velocity;
    private int emittingDelay;
    private int emittingCount;
    private double generationRandomDistance;

    public int getStart() {
        return start;
    }

    public ParticleEmitterConfig setStart(int start) {
        this.start = start;
        return this;
    }

    public int getTtl() {
        return ttl;
    }

    public ParticleEmitterConfig setTtl(int ttl) {
        this.ttl = ttl;
        return this;
    }

    public Vertex getVelocity() {
        return velocity;
    }

    public ParticleEmitterConfig setVelocity(Vertex velocity) {
        this.velocity = velocity;
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

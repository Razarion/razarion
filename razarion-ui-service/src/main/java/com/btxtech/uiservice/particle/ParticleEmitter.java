package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.particle.ParticleEmitterConfig;

import javax.inject.Inject;

/**
 * Created by Beat
 * 05.02.2017.
 */
public abstract class ParticleEmitter {
    @Inject
    private ParticleService particleService;
    private long lastGenerationTime;
    private Vertex position;
    private ParticleEmitterConfig particleEmitterConfig;

    protected abstract boolean isRunning(long timestamp);

    protected abstract Vertex updatePosition(double factor, Vertex position);

    public void init(Vertex position, ParticleEmitterConfig particleEmitterConfig) {
        this.position = position;
        this.particleEmitterConfig = particleEmitterConfig;
    }

    /**
     * @param timestamp timestamp
     * @param factor
     * @return true if emitter is not dead
     */
    public boolean tick(long timestamp, double factor) {
        if (!isRunning(timestamp)) {
            return false;
        }

        position = updatePosition(factor, position);

        // Emit particle
        if (lastGenerationTime + particleEmitterConfig.getEmittingDelay() < timestamp) {
            for (int i = 0; i < particleEmitterConfig.getEmittingCount(); i++) {
                double xRand = (Math.random() - 0.5) * particleEmitterConfig.getGenerationRandomDistance();
                double yRand = (Math.random() - 0.5) * particleEmitterConfig.getGenerationRandomDistance();
                particleService.addParticles(new Particle(timestamp, new Vertex(position.getX() + xRand, position.getY() + yRand, position.getZ()), particleEmitterConfig.getParticleConfig()));
            }
            lastGenerationTime = timestamp;
        }

        return true;
    }
}

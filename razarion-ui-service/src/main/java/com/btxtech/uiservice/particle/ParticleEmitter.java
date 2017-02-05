package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Vertex;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 05.02.2017.
 */
@Dependent
public class ParticleEmitter {
    @Inject
    private ParticleService particleService;
    private long lastTickTimestamp;
    private long lastGenerationTime;
    private long startTime;
    private Vertex position;
    private ParticleEmitterConfig particleEmitterConfig;

    public void init(long startTime, Vertex position, ParticleEmitterConfig particleEmitterConfig) {
        this.startTime = startTime;
        this.position = position;
        this.particleEmitterConfig = particleEmitterConfig;
    }

    /**
     * @param timestamp timestamp
     * @return true if emitter is not dead
     */
    public boolean tick(long timestamp) {
        if (startTime + particleEmitterConfig.getTtl() < timestamp) {
            return false;
        }
        // Update own position
        if (lastTickTimestamp > 0) {
            double factor = (timestamp - lastTickTimestamp) / 1000.0;
            position = position.add(particleEmitterConfig.getVelocity().multiply(factor));
        }
        lastTickTimestamp = timestamp;
        // Emit particle
        if (lastGenerationTime + particleEmitterConfig.getEmittingDelay() < timestamp) {
            for (int i = 0; i < particleEmitterConfig.getEmittingCount(); i++) {
                double xRand = Math.random() * particleEmitterConfig.getGenerationRandomDistance();
                double yRand = Math.random() * particleEmitterConfig.getGenerationRandomDistance();
                particleService.addParticles(new Particle(timestamp, new Vertex(position.getX() + xRand, position.getY() + yRand, position.getZ()), particleEmitterConfig.getVelocity()));
            }
            lastGenerationTime = timestamp;
        }

        return true;
    }
}

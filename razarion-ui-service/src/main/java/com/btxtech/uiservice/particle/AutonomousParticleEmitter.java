package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.particle.AutonomousParticleEmitterConfig;

import javax.enterprise.context.Dependent;

/**
 * Created by Beat
 * 06.02.2017.
 */
@Dependent
public class AutonomousParticleEmitter extends ParticleEmitter {
    private long startTimeStamp;
    private AutonomousParticleEmitterConfig particleEmitterConfig;
    private Vertex direction;

    public void init(long timestamp, Vertex position, Vertex particleDirection, Vertex direction, AutonomousParticleEmitterConfig autonomousParticleEmitterConfig) {
        this.direction = direction;
        super.init(position, particleDirection, autonomousParticleEmitterConfig);
        this.particleEmitterConfig = autonomousParticleEmitterConfig;
        startTimeStamp = timestamp + autonomousParticleEmitterConfig.getStartTime();
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    @Override
    protected boolean isRunning(long timestamp) {
        return startTimeStamp + particleEmitterConfig.getTimeToLive() >= timestamp;
    }

    @Override
    protected Vertex updatePosition(double factor, Vertex position) {
        if (direction != null && particleEmitterConfig.getDirectionSpeed() == null) {
            throw new IllegalStateException("AutonomousParticleEmitter.updatePosition() direction != null && particleEmitterConfig.getDirectionSpeed() == null");
        }
        if (direction != null) {
            return position.add(direction.normalize(factor * particleEmitterConfig.getDirectionSpeed()));
        }

        if (particleEmitterConfig.getVelocity() == null) {
            return position;
        }
        return position.add(particleEmitterConfig.getVelocity().multiply(factor));
    }
}

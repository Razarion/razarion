package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Vertex;

import javax.enterprise.context.Dependent;

/**
 * Created by Beat
 * 06.02.2017.
 */
@Dependent
public class AutonomousParticleEmitter extends ParticleEmitter {
    private long startTimeStamp;
    private long lastTickTimestamp;
    private AutonomousParticleEmitterConfig particleEmitterConfig;

    public void init(long timestamp, Vertex position, AutonomousParticleEmitterConfig autonomousParticleEmitterConfig) {
        super.init(position, autonomousParticleEmitterConfig);
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
        if (particleEmitterConfig.getVelocity() == null) {
            return position;
        }
        return position.add(particleEmitterConfig.getVelocity().multiply(factor));
    }
}

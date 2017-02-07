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

    public void init(long timestamp, Vertex position, AutonomousParticleEmitterConfig particleEmitterConfig) {
        super.init(position, particleEmitterConfig);
        this.particleEmitterConfig = particleEmitterConfig;
        startTimeStamp = timestamp + particleEmitterConfig.getStartTime();
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    @Override
    protected boolean isRunning(long timestamp) {
        return startTimeStamp + particleEmitterConfig.getTimeToLive() < timestamp;
    }

    @Override
    protected Vertex updatePosition(long timestamp, Vertex position) {
        Vertex newPosition = position;
        if (lastTickTimestamp > 0) {
            double factor = (timestamp - lastTickTimestamp) / 1000.0;
            newPosition = position.add(particleEmitterConfig.getVelocity().multiply(factor));
        }
        lastTickTimestamp = timestamp;
        return newPosition;
    }
}

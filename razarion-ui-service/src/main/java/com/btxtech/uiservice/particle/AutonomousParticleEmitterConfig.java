package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 06.02.2017.
 */
public class AutonomousParticleEmitterConfig extends ParticleEmitterConfig {
    // private Logger logger = Logger.getLogger(AutonomousParticleEmitterConfig.class.getName());
    private int startTime;
    private int timeToLive;
    private Vertex velocity;
    private Double directionSpeed;

    public int getStartTime() {
        return startTime;
    }

    public AutonomousParticleEmitterConfig setStartTime(int startTime) {
        this.startTime = startTime;
        return this;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public AutonomousParticleEmitterConfig setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
        return this;
    }

    public Vertex getVelocity() {
        return velocity;
    }

    public AutonomousParticleEmitterConfig setVelocity(Vertex velocity) {
        this.velocity = velocity;
        return this;
    }

    public Double getDirectionSpeed() {
        return directionSpeed;
    }

    public AutonomousParticleEmitterConfig setDirectionSpeed(Double directionSpeed) {
        this.directionSpeed = directionSpeed;
        return this;
    }
}

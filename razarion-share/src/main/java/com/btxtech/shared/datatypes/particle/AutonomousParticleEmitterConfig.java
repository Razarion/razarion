package com.btxtech.shared.datatypes.particle;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 06.02.2017.
 */
@Deprecated
public class AutonomousParticleEmitterConfig extends ParticleEmitterConfig<AutonomousParticleEmitterConfig> {
    // private Logger logger = Logger.getLogger(AutonomousParticleEmitterConfig.class.getName());
    private int startTime;
    private int timeToLive;
    private Vertex velocity;
    private Double directionSpeed;

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public Vertex getVelocity() {
        return velocity;
    }

    public void setVelocity(Vertex velocity) {
        this.velocity = velocity;
    }

    public Double getDirectionSpeed() {
        return directionSpeed;
    }

    public void setDirectionSpeed(Double directionSpeed) {
        this.directionSpeed = directionSpeed;
    }

    public AutonomousParticleEmitterConfig startTime(int startTime) {
        setStartTime(startTime);
        return this;
    }

    public AutonomousParticleEmitterConfig timeToLive(int timeToLive) {
        setTimeToLive(timeToLive);
        return this;
    }

    public AutonomousParticleEmitterConfig velocity(Vertex velocity) {
        setVelocity(velocity);
        return this;
    }

    public AutonomousParticleEmitterConfig directionSpeed(Double directionSpeed) {
        setDirectionSpeed(directionSpeed);
        return this;
    }
}

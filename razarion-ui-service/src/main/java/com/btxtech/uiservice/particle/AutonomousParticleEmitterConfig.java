package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 06.02.2017.
 */
public class AutonomousParticleEmitterConfig extends ParticleEmitterConfig {
    private int startTime;
    private int timeToLive;
    private Vertex velocity;

    public int getStartTime() {
        return startTime;
    }

    public ParticleEmitterConfig setStart(int start) {
        this.startTime = start;
        return this;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public ParticleEmitterConfig setTtl(int ttl) {
        this.timeToLive = ttl;
        return this;
    }

    public Vertex getVelocity() {
        return velocity;
    }

    public ParticleEmitterConfig setVelocity(Vertex velocity) {
        this.velocity = velocity;
        return this;
    }


}

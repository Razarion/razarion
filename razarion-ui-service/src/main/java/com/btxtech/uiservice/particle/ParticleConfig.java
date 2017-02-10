package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 07.02.2017.
 */
public class ParticleConfig {
    // private Logger logger = Logger.getLogger(ParticleConfig.class.getName());
    private int particleShapeConfigId;
    private int particleXColorRampOffsetIndex;
    private Double particleGrowTo;
    private Double particleGrowFrom;
    private int timeToLive;
    private Integer timeToLiveRandomPart;
    private Vertex velocity;
    private Vertex velocityRandomPart;
    private Vertex acceleration;

    public int getParticleShapeConfigId() {
        return particleShapeConfigId;
    }

    public ParticleConfig setParticleShapeConfigId(int particleShapeConfigId) {
        this.particleShapeConfigId = particleShapeConfigId;
        return this;
    }

    public int getParticleXColorRampOffsetIndex() {
        return particleXColorRampOffsetIndex;
    }

    public ParticleConfig setParticleXColorRampOffsetIndex(int particleXColorRampOffsetIndex) {
        this.particleXColorRampOffsetIndex = particleXColorRampOffsetIndex;
        return this;
    }

    public Double getParticleGrowTo() {
        return particleGrowTo;
    }

    public ParticleConfig setParticleGrowTo(Double particleGrowTo) {
        this.particleGrowTo = particleGrowTo;
        return this;
    }

    public Double getParticleGrowFrom() {
        return particleGrowFrom;
    }

    public ParticleConfig setParticleGrowFrom(Double particleGrowFrom) {
        this.particleGrowFrom = particleGrowFrom;
        return this;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public ParticleConfig setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
        return this;
    }

    public Integer getTimeToLiveRandomPart() {
        return timeToLiveRandomPart;
    }

    public ParticleConfig setTimeToLiveRandomPart(Integer timeToLiveRandomPart) {
        this.timeToLiveRandomPart = timeToLiveRandomPart;
        return this;
    }

    public Vertex getVelocity() {
        return velocity;
    }

    public ParticleConfig setVelocity(Vertex velocity) {
        this.velocity = velocity;
        return this;
    }

    public Vertex getVelocityRandomPart() {
        return velocityRandomPart;
    }

    public ParticleConfig setVelocityRandomPart(Vertex velocityRandomPart) {
        this.velocityRandomPart = velocityRandomPart;
        return this;
    }

    public Vertex getAcceleration() {
        return acceleration;
    }

    public ParticleConfig setAcceleration(Vertex acceleration) {
        this.acceleration = acceleration;
        return this;
    }
}

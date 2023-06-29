package com.btxtech.shared.datatypes.particle;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 07.02.2017.
 */
@Deprecated
public class ParticleConfig {
    // private Logger logger = Logger.getLogger(ParticleConfig.class.getName());
    private Integer particleShapeConfigId;
    private int particleXColorRampOffsetIndex;
    private Double particleGrowTo;
    private Double particleGrowFrom;
    private int timeToLive;
    private Integer timeToLiveRandomPart;
    private Vertex velocity;
    private Vertex velocityRandomPart;
    private Vertex acceleration;
    private Double directedVelocity;
    private Double directedVelocityRandomPart;
    private Double directedAcceleration;

    public Integer getParticleShapeConfigId() {
        return particleShapeConfigId;
    }

    public void setParticleShapeConfigId(Integer particleShapeConfigId) {
        this.particleShapeConfigId = particleShapeConfigId;
    }

    public int getParticleXColorRampOffsetIndex() {
        return particleXColorRampOffsetIndex;
    }

    public void setParticleXColorRampOffsetIndex(int particleXColorRampOffsetIndex) {
        this.particleXColorRampOffsetIndex = particleXColorRampOffsetIndex;
    }

    public Double getParticleGrowTo() {
        return particleGrowTo;
    }

    public void setParticleGrowTo(Double particleGrowTo) {
        this.particleGrowTo = particleGrowTo;
    }

    public Double getParticleGrowFrom() {
        return particleGrowFrom;
    }

    public void setParticleGrowFrom(Double particleGrowFrom) {
        this.particleGrowFrom = particleGrowFrom;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public Integer getTimeToLiveRandomPart() {
        return timeToLiveRandomPart;
    }

    public void setTimeToLiveRandomPart(Integer timeToLiveRandomPart) {
        this.timeToLiveRandomPart = timeToLiveRandomPart;
    }

    public Vertex getVelocity() {
        return velocity;
    }

    public void setVelocity(Vertex velocity) {
        this.velocity = velocity;
    }

    public Vertex getVelocityRandomPart() {
        return velocityRandomPart;
    }

    public void setVelocityRandomPart(Vertex velocityRandomPart) {
        this.velocityRandomPart = velocityRandomPart;
    }

    public Vertex getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vertex acceleration) {
        this.acceleration = acceleration;
    }

    public Double getDirectedVelocity() {
        return directedVelocity;
    }

    public void setDirectedVelocity(Double directedVelocity) {
        this.directedVelocity = directedVelocity;
    }

    public Double getDirectedVelocityRandomPart() {
        return directedVelocityRandomPart;
    }

    public void setDirectedVelocityRandomPart(Double directedVelocityRandomPart) {
        this.directedVelocityRandomPart = directedVelocityRandomPart;
    }

    public Double getDirectedAcceleration() {
        return directedAcceleration;
    }

    public void setDirectedAcceleration(Double directedAcceleration) {
        this.directedAcceleration = directedAcceleration;
    }

    public ParticleConfig particleShapeConfigId(Integer particleShapeConfigId) {
        setParticleShapeConfigId(particleShapeConfigId);
        return this;
    }

    public ParticleConfig particleXColorRampOffsetIndex(int particleXColorRampOffsetIndex) {
        setParticleXColorRampOffsetIndex(particleXColorRampOffsetIndex);
        return this;
    }

    public ParticleConfig particleGrowTo(Double particleGrowTo) {
        setParticleGrowTo(particleGrowTo);
        return this;
    }

    public ParticleConfig particleGrowFrom(Double particleGrowFrom) {
        setParticleGrowFrom(particleGrowFrom);
        return this;
    }

    public ParticleConfig timeToLive(int timeToLive) {
        setTimeToLive(timeToLive);
        return this;
    }

    public ParticleConfig timeToLiveRandomPart(Integer timeToLiveRandomPart) {
        setTimeToLiveRandomPart(timeToLiveRandomPart);
        return this;
    }

    public ParticleConfig velocity(Vertex velocity) {
        setVelocity(velocity);
        return this;
    }

    public ParticleConfig velocityRandomPart(Vertex velocityRandomPart) {
        setVelocityRandomPart(velocityRandomPart);
        return this;
    }

    public ParticleConfig acceleration(Vertex acceleration) {
        setAcceleration(acceleration);
        return this;
    }

    public ParticleConfig directedVelocity(Double directedVelocity) {
        setDirectedVelocity(directedVelocity);
        return this;
    }

    public ParticleConfig directedVelocityRandomPart(Double directedVelocityRandomPart) {
        setDirectedVelocityRandomPart(directedVelocityRandomPart);
        return this;
    }

    public ParticleConfig directedAcceleration(Double directedAcceleration) {
        setDirectedAcceleration(directedAcceleration);
        return this;
    }
}

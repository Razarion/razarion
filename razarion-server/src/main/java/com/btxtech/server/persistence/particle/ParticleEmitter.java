package com.btxtech.server.persistence.particle;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.particle.ParticleConfig;
import com.btxtech.shared.datatypes.particle.ParticleEmitterConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ParticleEmitter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    private int emittingDelay;
    private int emittingCount;
    private double generationRandomDistance;
    private int particleXColorRampOffsetIndex;
    private Double particleGrowTo;
    private Double particleGrowFrom;
    private int particleTimeToLive;
    private Integer particleTimeToLiveRandomPart;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "particleVelocityX")),
            @AttributeOverride(name = "y", column = @Column(name = "particleVelocityY")),
            @AttributeOverride(name = "z", column = @Column(name = "particleVelocityZ")),
    })
    private Vertex particleVelocity;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "particleVelocityRandomPartX")),
            @AttributeOverride(name = "y", column = @Column(name = "particleVelocityRandomPartY")),
            @AttributeOverride(name = "z", column = @Column(name = "particleVelocityRandomPartZ")),
    })
    private Vertex particleVelocityRandomPart;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "particleAccelerationX")),
            @AttributeOverride(name = "y", column = @Column(name = "particleAccelerationY")),
            @AttributeOverride(name = "z", column = @Column(name = "particleAccelerationZ")),
    })
    private Vertex particleAcceleration;


    protected void fill(ParticleEmitterConfig config) {
        config.id(id)
                .internalName(internalName)
                .emittingDelay(emittingDelay)
                .emittingCount(emittingCount)
                .generationRandomDistance(generationRandomDistance)
                .particleConfig(new ParticleConfig()
                        .particleXColorRampOffsetIndex(particleXColorRampOffsetIndex)
                        .particleGrowTo(particleGrowTo)
                        .particleGrowFrom(particleGrowFrom)
                        .timeToLive(particleTimeToLive)
                        .timeToLiveRandomPart(particleTimeToLiveRandomPart)
                        .velocity(particleVelocity)
                        .velocityRandomPart(particleVelocityRandomPart)
                        .acceleration(particleAcceleration));
    }

    protected void fromConfig(ParticleEmitterConfig config) {
        id = config.getId();
        internalName = config.getInternalName();
        emittingDelay = config.getEmittingDelay();
        emittingCount = config.getEmittingCount();
        generationRandomDistance = config.getGenerationRandomDistance();
        if (config.getParticleConfig() != null) {
            particleXColorRampOffsetIndex = config.getParticleConfig().getParticleXColorRampOffsetIndex();
            particleGrowTo = config.getParticleConfig().getParticleGrowTo();
            particleGrowFrom = config.getParticleConfig().getParticleGrowFrom();
            particleTimeToLive = config.getParticleConfig().getTimeToLive();
            particleTimeToLiveRandomPart = config.getParticleConfig().getTimeToLiveRandomPart();
            particleVelocity = config.getParticleConfig().getVelocity();
            particleVelocityRandomPart = config.getParticleConfig().getVelocityRandomPart();
            particleAcceleration = config.getParticleConfig().getAcceleration();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParticleEmitter that = (ParticleEmitter) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}

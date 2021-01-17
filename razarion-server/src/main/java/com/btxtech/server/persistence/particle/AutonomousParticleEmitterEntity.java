package com.btxtech.server.persistence.particle;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.particle.AutonomousParticleEmitterConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "PARTICLE_EMITTER_AUTONOMOUS")
public class AutonomousParticleEmitterEntity extends ParticleEmitter {
    private int startTime;
    private int timeToLive;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "velocityX")),
            @AttributeOverride(name = "y", column = @Column(name = "velocityY")),
            @AttributeOverride(name = "z", column = @Column(name = "velocityZ")),
    })
    private Vertex velocity;
    private Double directionSpeed;

    public AutonomousParticleEmitterConfig toConfig() {
        AutonomousParticleEmitterConfig autonomousParticleEmitterConfig = new AutonomousParticleEmitterConfig()
                .startTime(startTime)
                .timeToLive(timeToLive)
                .velocity(velocity)
                .directionSpeed(directionSpeed);
        fill(autonomousParticleEmitterConfig);
        return autonomousParticleEmitterConfig;
    }

    public void fromConfig(AutonomousParticleEmitterConfig config, ParticleShapeCrudPersistence particleShapeCrudPersistence) {
        super.fromConfig(config, particleShapeCrudPersistence);
        startTime = config.getStartTime();
        timeToLive = config.getTimeToLive();
        velocity = config.getVelocity();
        directionSpeed = config.getDirectionSpeed();
    }

}

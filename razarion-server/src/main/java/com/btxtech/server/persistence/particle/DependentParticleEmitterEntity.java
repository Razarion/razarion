package com.btxtech.server.persistence.particle;

import com.btxtech.shared.datatypes.particle.DependentParticleEmitterConfig;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "PARTICLE_EMITTER_DEPENDENT")
public class DependentParticleEmitterEntity extends ParticleEmitter {

    public DependentParticleEmitterConfig toConfig() {
        DependentParticleEmitterConfig dependentParticleEmitterConfig = new DependentParticleEmitterConfig();
        fill(dependentParticleEmitterConfig);
        return dependentParticleEmitterConfig;
    }

    public void fromConfig(DependentParticleEmitterConfig config) {
        super.fromConfig(config);
    }
}

package com.btxtech.server.persistence.particle;

import com.btxtech.server.persistence.AudioLibraryEntity;
import com.btxtech.server.persistence.AudioPersistence;
import com.btxtech.server.persistence.PersistenceUtil;
import com.btxtech.shared.datatypes.particle.ParticleEmitterSequenceConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

import static com.btxtech.server.persistence.AudioPersistence.toIds;
import static com.btxtech.server.persistence.PersistenceUtil.toConfigList;

@Entity
@Table(name = "PARTICLE_EMITTER_SEQUENCE")
public class ParticleEmitterSequenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "particleEmitterSequence", nullable = false)
    private List<DependentParticleEmitterEntity> dependent;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "particleEmitterSequence", nullable = false)
    private List<AutonomousParticleEmitterEntity> autonomous;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "PARTICLE_EMITTER_SEQUENCE_AUDIO",
            joinColumns = @JoinColumn(name = "particleEmitterSequence"),
            inverseJoinColumns = @JoinColumn(name = "audio"))
    private List<AudioLibraryEntity> audios;

    public Integer getId() {
        return id;
    }

    public ParticleEmitterSequenceConfig toConfig() {
        return new ParticleEmitterSequenceConfig()
                .id(id)
                .internalName(internalName)
                .dependent(toConfigList(dependent, DependentParticleEmitterEntity::toConfig))
                .autonomous(toConfigList(autonomous, AutonomousParticleEmitterEntity::toConfig))
                .audioIds(toIds(audios));
    }

    public void fromConfig(ParticleEmitterSequenceConfig config, AudioPersistence audioPersistence, ParticleShapeCrudPersistence particleShapeCrudPersistence) {
        internalName = config.getInternalName();
        dependent = PersistenceUtil.fromConfigs(dependent, config.getDependent(), DependentParticleEmitterEntity::new, (dependentParticleEmitterEntity, config1) -> dependentParticleEmitterEntity.fromConfig(config1, particleShapeCrudPersistence));
        autonomous = PersistenceUtil.fromConfigs(autonomous, config.getAutonomous(), AutonomousParticleEmitterEntity::new, (dependentParticleEmitterEntity, config1) -> dependentParticleEmitterEntity.fromConfig(config1, particleShapeCrudPersistence));
        audios = audioPersistence.toAudioLibraryEntities(config.getAudioIds());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParticleEmitterSequenceEntity that = (ParticleEmitterSequenceEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}

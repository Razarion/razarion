package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.particle.ParticleEmitterSequenceCrudPersistence;
import com.btxtech.server.persistence.particle.ParticleEmitterSequenceEntity;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionParticleConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;

/**
 * Created by Beat
 * 19.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_DEMOLITION_STEP_EFFECT_PARTICLE")
public class DemolitionStepEffectParticleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ParticleEmitterSequenceEntity particle;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "positionX")),
            @AttributeOverride(name = "y", column = @Column(name = "positionY")),
            @AttributeOverride(name = "z", column = @Column(name = "positionZ")),
    })
    private Vertex position;

    public DemolitionParticleConfig toDemolitionParticleConfig() {
        return new DemolitionParticleConfig()
                .particleConfigId(extractId(particle, ParticleEmitterSequenceEntity::getId))
                .position(position);
    }

    public void fromDemolitionParticleConfig(DemolitionParticleConfig demolitionParticleConfig, ParticleEmitterSequenceCrudPersistence particleEmitterSequenceCrudPersistence) {
        particle = particleEmitterSequenceCrudPersistence.getEntity(demolitionParticleConfig.getParticleConfigId());
        position = demolitionParticleConfig.getPosition();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DemolitionStepEffectParticleEntity that = (DemolitionStepEffectParticleEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}

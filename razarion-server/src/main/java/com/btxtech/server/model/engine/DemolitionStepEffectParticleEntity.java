package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionParticleConfig;

import jakarta.persistence.*;

/**
 * Created by Beat
 * 19.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_DEMOLITION_STEP_EFFECT_PARTICLE")
public class DemolitionStepEffectParticleEntity extends BaseEntity {
    
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "positionX")),
            @AttributeOverride(name = "y", column = @Column(name = "positionY")),
            @AttributeOverride(name = "z", column = @Column(name = "positionZ")),
    })
    private Vertex position;

    public DemolitionParticleConfig toDemolitionParticleConfig() {
        return new DemolitionParticleConfig()
                .position(position);
    }

    public void fromDemolitionParticleConfig() {
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
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}

package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionParticleConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 19.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_DEMOLITION_STEP_EFFECT")
public class DemolitionStepEffectEntity extends BaseEntity {
    
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "demolitionStepEffect", nullable = false)
    @OrderColumn(name = "orderColumn")
    private List<DemolitionStepEffectParticleEntity> demolitionParticleConfigs;

    public DemolitionStepEffect toDemolitionStepEffect() {
        DemolitionStepEffect demolitionStepEffect = new DemolitionStepEffect();
        if (this.demolitionParticleConfigs != null && !this.demolitionParticleConfigs.isEmpty()) {
            List<DemolitionParticleConfig> demolitionParticleConfigs = new ArrayList<>();
            for (DemolitionStepEffectParticleEntity demolitionParticleConfig : this.demolitionParticleConfigs) {
                demolitionParticleConfigs.add(demolitionParticleConfig.toDemolitionParticleConfig());
            }
        }
        return demolitionStepEffect;
    }

    public void fromDemolitionStepEffect(DemolitionStepEffect demolitionStepEffect) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DemolitionStepEffectEntity that = (DemolitionStepEffectEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}

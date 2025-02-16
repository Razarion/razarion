package com.btxtech.server.persistence.itemtype;

import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionParticleConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 19.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_DEMOLITION_STEP_EFFECT")
public class DemolitionStepEffectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}

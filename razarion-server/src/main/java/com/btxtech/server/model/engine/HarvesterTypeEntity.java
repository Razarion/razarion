package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.ParticleSystemEntity;
import com.btxtech.server.service.ui.ParticleSystemService;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import jakarta.persistence.*;

import static com.btxtech.server.service.PersistenceUtil.extractId;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_HARVESTER_TYPE")
public class HarvesterTypeEntity extends BaseEntity {

    private int harvestRange;
    private double progress;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ParticleSystemEntity particleSystem;

    public HarvesterType toHarvesterType() {
        return new HarvesterType()
                .range(harvestRange)
                .progress(progress)
                .particleSystemConfigId(extractId(particleSystem, ParticleSystemEntity::getId));
    }

    public void fromHarvesterType(HarvesterType harvesterType, ParticleSystemService particleSystemCrudPersistence) {
        harvestRange = harvesterType.getRange();
        progress = harvesterType.getProgress();
        particleSystem = particleSystemCrudPersistence.getEntity(harvesterType.getParticleSystemConfigId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HarvesterTypeEntity that = (HarvesterTypeEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}

package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.ParticleSystemCrudPersistence;
import com.btxtech.server.persistence.ui.ParticleSystemEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;

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
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_HARVESTER_TYPE")
public class HarvesterTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
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

    public void fromHarvesterType(HarvesterType harvesterType, ParticleSystemCrudPersistence particleSystemCrudPersistence) {
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}

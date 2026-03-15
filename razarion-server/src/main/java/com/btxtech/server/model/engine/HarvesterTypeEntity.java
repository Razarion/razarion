package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_HARVESTER_TYPE")
public class HarvesterTypeEntity extends BaseEntity {

    private int harvestRange;
    private double progress;

    public HarvesterType toHarvesterType() {
        return new HarvesterType()
                .range(harvestRange)
                .progress(progress);
    }

    public void fromHarvesterType(HarvesterType harvesterType) {
        harvestRange = harvesterType.getRange();
        progress = harvesterType.getProgress();
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

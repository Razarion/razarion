package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.SpecialType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_SPECIAL_TYPE")
public class SpecialTypeEntity extends BaseEntity {
    private boolean miniTerrain;

    public SpecialType toSpecialType() {
        return new SpecialType().setMiniTerrain(miniTerrain);
    }

    public void fromSpecialType(SpecialType specialType) {
        miniTerrain = specialType.isMiniTerrain();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SpecialTypeEntity that = (SpecialTypeEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}

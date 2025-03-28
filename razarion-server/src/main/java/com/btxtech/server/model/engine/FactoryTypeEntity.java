package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.service.engine.BaseItemTypeCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_FACTORY_TYPE")
public class FactoryTypeEntity extends BaseEntity {

    private double progress;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "BASE_ITEM_FACTORY_TYPE_ABLE_TO_BUILD",
            joinColumns = @JoinColumn(name = "factory"),
            inverseJoinColumns = @JoinColumn(name = "baseItemType"))
    private List<BaseItemTypeEntity> ableToBuilds;

    public FactoryType toFactoryType() {
        FactoryType factoryType = new FactoryType().setProgress(progress);
        if (ableToBuilds != null && !ableToBuilds.isEmpty()) {
            List<Integer> ableToBuildIds = new ArrayList<>();
            for (BaseItemTypeEntity ableToBuild : ableToBuilds) {
                ableToBuildIds.add(ableToBuild.getId());
            }
            factoryType.setAbleToBuildIds(ableToBuildIds);
        }
        return factoryType;
    }

    public void fromFactoryTypeEntity(FactoryType factoryType, BaseItemTypeCrudPersistence baseItemTypeCrudPersistence) {
        progress = factoryType.getProgress();
        if (factoryType.getAbleToBuildIds() != null && !factoryType.getAbleToBuildIds().isEmpty()) {
            if (ableToBuilds == null) {
                ableToBuilds = new ArrayList<>();
            }
            ableToBuilds.clear();
            for (Integer ableToBuildId : factoryType.getAbleToBuildIds()) {
                ableToBuilds.add(baseItemTypeCrudPersistence.getEntity(ableToBuildId));
            }
        } else {
            ableToBuilds = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FactoryTypeEntity that = (FactoryTypeEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}

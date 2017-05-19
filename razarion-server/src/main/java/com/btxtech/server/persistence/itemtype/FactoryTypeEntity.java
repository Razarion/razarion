package com.btxtech.server.persistence.itemtype;

import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_FACTORY_TYPE")
public class FactoryTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
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

    public void fromFactoryTypeEntity(FactoryType factoryType, ItemTypePersistence itemTypePersistence) {
        progress = factoryType.getProgress();
        if (factoryType.getAbleToBuildIds() != null && !factoryType.getAbleToBuildIds().isEmpty()) {
            if (ableToBuilds == null) {
                ableToBuilds = new ArrayList<>();
            }
            ableToBuilds.clear();
            for (int ableToBuildId : factoryType.getAbleToBuildIds()) {
                ableToBuilds.add(itemTypePersistence.readBaseItemTypeEntity(ableToBuildId));
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}

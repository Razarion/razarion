package com.btxtech.server.persistence.itemtype;

import com.btxtech.shared.gameengine.datatypes.itemtype.ItemContainerType;

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
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_ITEM_CONTAINER_TYPE")
public class ItemContainerTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int maxCount;
    private double itemRange;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "BASE_ITEM_ITEM_CONTAINER_TYPE_ABLE_TO_CONTAIN",
            joinColumns = @JoinColumn(name = "container"),
            inverseJoinColumns = @JoinColumn(name = "baseItemType"))
    private List<BaseItemTypeEntity> ableToContain;

    public ItemContainerType toItemContainerType() {
        ItemContainerType itemContainerType = new ItemContainerType().setRange(itemRange).setMaxCount(maxCount);
        if (ableToContain != null && !ableToContain.isEmpty()) {
            itemContainerType.setAbleToContain(ableToContain.stream().map(BaseItemTypeEntity::getId).collect(Collectors.toList()));
        }
        return itemContainerType;
    }

    public void fromItemContainerType(ItemContainerType itemContainerType, BaseItemTypeCrudPersistence baseItemTypeCrudPersistence) {
        maxCount = itemContainerType.getMaxCount();
        itemRange = itemContainerType.getRange();
        if (itemContainerType.getAbleToContain() != null && !itemContainerType.getAbleToContain().isEmpty()) {
            if (ableToContain == null) {
                ableToContain = new ArrayList<>();
            }
            ableToContain.clear();
            itemContainerType.getAbleToContain().forEach(ableToBuildId -> ableToContain.add(baseItemTypeCrudPersistence.getEntity(ableToBuildId)));
        } else {
            ableToContain = null;
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

        ItemContainerTypeEntity that = (ItemContainerTypeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}

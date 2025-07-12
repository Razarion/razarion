package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.service.engine.BaseItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemContainerType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_ITEM_CONTAINER_TYPE")
public class ItemContainerTypeEntity extends BaseEntity {

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

    public void fromItemContainerType(ItemContainerType itemContainerType, BaseItemTypeService baseItemTypeCrudPersistence) {
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
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}

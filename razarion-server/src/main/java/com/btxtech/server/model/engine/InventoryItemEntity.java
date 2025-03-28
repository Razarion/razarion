package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import jakarta.persistence.*;

import static com.btxtech.server.service.PersistenceUtil.extractId;


/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "INVENTORY_ITEM")
public class InventoryItemEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity baseItemType;
    private int baseItemTypeCount;
    private double itemFreeRange;
    private Integer razarion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity image;
    private Integer crystalCost;


    public InventoryItem toInventoryItem() {
        InventoryItem inventoryItem = new InventoryItem()
                .id(getId())
                .internalName(getInternalName())
                .baseItemTypeId(extractId(baseItemType, BaseItemTypeEntity::getId))
                .baseItemTypeCount(baseItemTypeCount)
                .baseItemTypeFreeRange(itemFreeRange)
                .razarion(razarion)
                .crystalCost(crystalCost);

        if (image != null) {
            inventoryItem.imageId(image.getId());
        }
        return inventoryItem;
    }

    public void fromInventoryItem(InventoryItem inventoryItem) {
        baseItemTypeCount = inventoryItem.getBaseItemTypeCount();
        itemFreeRange = inventoryItem.getBaseItemTypeFreeRange();
        razarion = inventoryItem.getRazarion();
        crystalCost = inventoryItem.getCrystalCost();
    }

    public void setBaseItemType(BaseItemTypeEntity baseItemType) {
        this.baseItemType = baseItemType;
    }

    public void setImage(ImageLibraryEntity image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InventoryItemEntity that = (InventoryItemEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }

}

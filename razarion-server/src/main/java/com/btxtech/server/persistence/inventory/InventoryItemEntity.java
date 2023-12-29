package com.btxtech.server.persistence.inventory;

import com.btxtech.server.persistence.I18nBundleEntity;
import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "INVENTORY_ITEM")
public class InventoryItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nName;
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

    public Integer getId() {
        return id;
    }

    public InventoryItem toInventoryItem() {
        InventoryItem inventoryItem = new InventoryItem()
                .id(id)
                .internalName(internalName)
                .baseItemTypeId(extractId(baseItemType, BaseItemTypeEntity::getId))
                .baseItemTypeCount(baseItemTypeCount)
                .baseItemTypeFreeRange(itemFreeRange)
                .razarion(razarion)
                .crystalCost(crystalCost);
        if (i18nName != null) {
            inventoryItem.i18nName(i18nName.toI18nString());
        }

        if (image != null) {
            inventoryItem.imageId(image.getId());
        }
        return inventoryItem;
    }

    public void fromInventoryItem(InventoryItem inventoryItem) {
        internalName = inventoryItem.getInternalName();
        i18nName = I18nBundleEntity.fromI18nStringSafe(inventoryItem.getI18nName(), i18nName);
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

    public String getInternalName() {
        return internalName;
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}

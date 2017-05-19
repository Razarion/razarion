package com.btxtech.server.persistence.inventory;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.tracker.I18nBundleEntity;
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
    private String name;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity baseItemType;
    private int baseItemTypeCount;
    private double itemFreeRange;
    private Integer gold;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity image;

    public Integer getId() {
        return id;
    }

    public InventoryItem toInventoryItem() {
        InventoryItem inventoryItem = new InventoryItem().setId(id).setName(name);
        if (baseItemType != null) {
            inventoryItem.setBaseItemType(baseItemType.getId()).setBaseItemTypeCount(baseItemTypeCount).setItemFreeRange(itemFreeRange);
        }
        if (i18nName != null) {
            inventoryItem.setI18nName(i18nName.toI18nString());
        }
        inventoryItem.setGold(gold);
        if (image != null) {
            inventoryItem.setImageId(image.getId());
        }
        return inventoryItem;
    }

    public void fromInventoryItem(InventoryItem inventoryItem) {
        name = inventoryItem.getName();
        // TODO i18nName
        baseItemTypeCount = inventoryItem.getBaseItemTypeCount();
        itemFreeRange = inventoryItem.getItemFreeRange();
        gold = inventoryItem.getGold();
    }

    public BaseItemTypeEntity getBaseItemType() {
        return baseItemType;
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}

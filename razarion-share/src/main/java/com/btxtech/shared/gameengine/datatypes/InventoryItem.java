package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;

/**
 * Created by Beat
 * 25.10.2016.
 */
public class InventoryItem implements ObjectNameIdProvider {
    private int id;
    private I18nString i18nName;
    private String internalName;
    private Integer gold;
    private Integer baseItemTypeId;
    private int baseItemTypeCount;
    private double baseItemTypeFreeRange;
    private Integer imageId;

    public int getId() {
        return id;
    }

    public InventoryItem setId(int id) {
        this.id = id;
        return this;
    }

    public I18nString getI18nName() {
        return i18nName;
    }

    public InventoryItem setI18nName(I18nString i18nName) {
        this.i18nName = i18nName;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public InventoryItem setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public Integer getGold() {
        return gold;
    }

    public InventoryItem setGold(Integer gold) {
        this.gold = gold;
        return this;
    }

    public Integer getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public InventoryItem setBaseItemTypeId(Integer baseItemTypeId) {
        this.baseItemTypeId = baseItemTypeId;
        return this;
    }

    public int getBaseItemTypeCount() {
        return baseItemTypeCount;
    }

    public InventoryItem setBaseItemTypeCount(int baseItemTypeCount) {
        this.baseItemTypeCount = baseItemTypeCount;
        return this;
    }

    public double getBaseItemTypeFreeRange() {
        return baseItemTypeFreeRange;
    }

    public InventoryItem setBaseItemTypeFreeRange(double baseItemTypeFreeRange) {
        this.baseItemTypeFreeRange = baseItemTypeFreeRange;
        return this;
    }

    public Integer getImageId() {
        return imageId;
    }

    public InventoryItem setImageId(Integer imageId) {
        this.imageId = imageId;
        return this;
    }

    public boolean hasBaseItemTypeId() {
        return baseItemTypeId != null;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) { // equals needed because Errai binder proxy
            return false;
        }

        InventoryItem that = (InventoryItem) o;

        return getId() == that.getId(); // itemType.getId() needed because Errai binder proxy
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "id=" + id +
                ", name='" + internalName + '\'' +
                ", gold=" + gold +
                ", baseItemTypeId=" + baseItemTypeId +
                ", baseItemTypeCount=" + baseItemTypeCount +
                ", baseItemTypeFreeRange=" + baseItemTypeFreeRange +
                ", imageId=" + imageId +
                '}';
    }
}

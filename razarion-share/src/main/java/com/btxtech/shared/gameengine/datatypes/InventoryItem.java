package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.datatypes.I18nString;

/**
 * Created by Beat
 * 25.10.2016.
 */
public class InventoryItem {
    private Integer id;
    private I18nString i18nName;
    private String name;
    private Integer gold;
    private Integer baseItemType;
    private int baseItemTypeCount;
    private double itemFreeRange;
    private Integer imageId;

    public Integer getId() {
        return id;
    }

    public InventoryItem setId(Integer id) {
        this.id = id;
        return this;
    }

    public I18nString getI18nName() {
        return i18nName;
    }

    public void setI18nName(I18nString i18nName) {
        this.i18nName = i18nName;
    }

    public String getName() {
        return name;
    }

    public InventoryItem setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getGold() {
        return gold;
    }

    public InventoryItem setGold(Integer gold) {
        this.gold = gold;
        return this;
    }

    public Integer getBaseItemType() {
        return baseItemType;
    }

    public InventoryItem setBaseItemType(Integer baseItemType) {
        this.baseItemType = baseItemType;
        return this;
    }

    public int getBaseItemTypeCount() {
        return baseItemTypeCount;
    }

    public InventoryItem setBaseItemTypeCount(int baseItemTypeCount) {
        this.baseItemTypeCount = baseItemTypeCount;
        return this;
    }

    public double getItemFreeRange() {
        return itemFreeRange;
    }

    public InventoryItem setItemFreeRange(double itemFreeRange) {
        this.itemFreeRange = itemFreeRange;
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
        return baseItemType != null;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gold=" + gold +
                ", baseItemType=" + baseItemType +
                ", baseItemTypeCount=" + baseItemTypeCount +
                ", itemFreeRange=" + itemFreeRange +
                ", imageId=" + imageId +
                '}';
    }
}

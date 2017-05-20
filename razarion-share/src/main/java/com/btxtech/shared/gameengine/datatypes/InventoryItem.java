package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.datatypes.I18nString;

/**
 * Created by Beat
 * 25.10.2016.
 */
public class InventoryItem {
    private int id;
    private I18nString i18nName;
    private String name;
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
    public String toString() {
        return "InventoryItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gold=" + gold +
                ", baseItemTypeId=" + baseItemTypeId +
                ", baseItemTypeCount=" + baseItemTypeCount +
                ", baseItemTypeFreeRange=" + baseItemTypeFreeRange +
                ", imageId=" + imageId +
                '}';
    }
}

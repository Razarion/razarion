package com.btxtech.shared.gameengine.datatypes;

/**
 * Created by Beat
 * 25.10.2016.
 */
public class InventoryItem {
    private Integer id;
    private String name;
    private Integer gold;
    private Integer baseItemType;
    private int baseItemTypeCount;
    private double itemFreeRange;

    public Integer getId() {
        return id;
    }

    public InventoryItem setId(Integer id) {
        this.id = id;
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
}

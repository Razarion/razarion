package com.btxtech.shared.gameengine.datatypes.itemtype;

/**
 * Created by Beat
 * 25.10.2016.
 */
public class BoxItemTypePossibility {
    private int id;
    private double possibility;
    private Integer inventoryItemId;

    public int getId() {
        return id;
    }

    public double getPossibility() {
        return possibility;
    }

    public Integer getInventoryItemId() {
        return inventoryItemId;
    }

    public BoxItemTypePossibility id(int id) {
        this.id = id;
        return this;
    }

    public BoxItemTypePossibility possibility(double possibility) {
        this.possibility = possibility;
        return this;
    }

    public BoxItemTypePossibility inventoryItem(Integer inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
        return this;
    }
}

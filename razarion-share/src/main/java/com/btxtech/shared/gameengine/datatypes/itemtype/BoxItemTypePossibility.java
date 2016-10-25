package com.btxtech.shared.gameengine.datatypes.itemtype;

/**
 * Created by Beat
 * 25.10.2016.
 */
public class BoxItemTypePossibility {
    private int id;
    private double possibility;
    private InventoryItem inventoryItem;

    public int getId() {
        return id;
    }

    public double getPossibility() {
        return possibility;
    }

    public InventoryItem getInventoryItem() {
        return inventoryItem;
    }

    public BoxItemTypePossibility id(int id) {
        this.id = id;
        return this;
    }

    public BoxItemTypePossibility possibility(double possibility) {
        this.possibility = possibility;
        return this;
    }

    public BoxItemTypePossibility inventoryItem(InventoryItem inventoryItem) {
        this.inventoryItem = inventoryItem;
        return this;
    }
}

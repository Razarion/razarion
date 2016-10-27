package com.btxtech.shared.gameengine.datatypes.itemtype;

/**
 * Created by Beat
 * 25.10.2016.
 */
public class BoxItemTypePossibility {
    private double possibility;
    private Integer inventoryItemId;

    public double getPossibility() {
        return possibility;
    }

    public BoxItemTypePossibility setPossibility(double possibility) {
        this.possibility = possibility;
        return this;
    }

    public Integer getInventoryItemId() {
        return inventoryItemId;
    }

    public BoxItemTypePossibility setInventoryItemId(Integer inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
        return this;
    }
}

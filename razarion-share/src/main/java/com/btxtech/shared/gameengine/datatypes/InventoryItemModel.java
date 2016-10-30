package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.gameengine.datatypes.InventoryItem;

/**
 * Created by Beat
 * 29.10.2016.
 */
public class InventoryItemModel {
    private InventoryItem inventoryItem;
    private int itemCount;

    public InventoryItemModel(InventoryItem inventoryItem) {
        this.inventoryItem = inventoryItem;
    }

    public void increaseItemCount() {
        itemCount++;
    }

    public InventoryItem getInventoryItem() {
        return inventoryItem;
    }

    public int getItemCount() {
        return itemCount;
    }
}

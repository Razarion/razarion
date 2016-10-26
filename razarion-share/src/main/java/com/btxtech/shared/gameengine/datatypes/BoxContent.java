package com.btxtech.shared.gameengine.datatypes;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 26.10.2016.
 */
public class BoxContent {
    private Collection<InventoryItem> inventoryItems = new ArrayList<>();

    public void addInventoryItem(InventoryItem inventoryItem) {
        inventoryItems.add(inventoryItem);
    }

    public Collection<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }
}

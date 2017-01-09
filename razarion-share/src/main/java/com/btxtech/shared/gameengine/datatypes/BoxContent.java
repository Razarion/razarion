package com.btxtech.shared.gameengine.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 26.10.2016.
 */
public class BoxContent {
    private List<InventoryItem> inventoryItems = new ArrayList<>();

    public void addInventoryItem(InventoryItem inventoryItem) {
        inventoryItems.add(inventoryItem);
    }

    public List<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }
}

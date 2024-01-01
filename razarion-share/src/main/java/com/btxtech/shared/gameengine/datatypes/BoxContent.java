package com.btxtech.shared.gameengine.datatypes;

import jsinterop.annotations.JsType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 26.10.2016.
 */
@JsType
public class BoxContent {
    private final List<InventoryItem> inventoryItems = new ArrayList<>();
    private int crystals;

    public void addInventoryItem(InventoryItem inventoryItem) {
        inventoryItems.add(inventoryItem);
    }

    public List<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }

    public void addCrystals(int crystals) {
        this.crystals += crystals;
    }

    public int getCrystals() {
        return crystals;
    }
}

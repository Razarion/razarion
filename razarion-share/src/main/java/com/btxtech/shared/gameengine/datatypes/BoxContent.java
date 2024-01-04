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

    @SuppressWarnings("unused") // Used by angular
    // Do not call this method get<something>, because it will be sent as json via websocket to the client
    public InventoryItem[] toInventoryItemArray() {
        return inventoryItems.toArray(new InventoryItem[0]);
    }

    public void addCrystals(int crystals) {
        this.crystals += crystals;
    }

    public int getCrystals() {
        return crystals;
    }
}

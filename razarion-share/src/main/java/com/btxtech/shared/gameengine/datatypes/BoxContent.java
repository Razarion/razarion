package com.btxtech.shared.gameengine.datatypes;

import jsinterop.annotations.JsType;
import org.dominokit.jackson.annotation.JSONMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 26.10.2016.
 */
@JsType
@JSONMapper
public class BoxContent {
    private List<InventoryItem> inventoryItems = new ArrayList<>();
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

    public void setInventoryItems(List<InventoryItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    public void setCrystals(int crystals) {
        this.crystals = crystals;
    }
}

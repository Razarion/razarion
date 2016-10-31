package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.InventoryItem;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Beat
 * 25.10.2016.
 */
@ApplicationScoped
public class InventoryService {
    private final HashMap<Integer, InventoryItem> inventoryItems = new HashMap<>();

    public void onGameEngineInit(@Observes GameEngineInitEvent engineInitEvent) {
        setInventoryItems(engineInitEvent.getGameEngineConfig().getInventoryItems());
    }

    private void setInventoryItems(List<InventoryItem> inventoryItems) {
        this.inventoryItems.clear();
        if (inventoryItems != null) {
            for (InventoryItem inventoryItem : inventoryItems) {
                this.inventoryItems.put(inventoryItem.getId(), inventoryItem);
            }
        }
    }

    public InventoryItem getInventoryItem(int id) {
        InventoryItem inventoryItem = inventoryItems.get(id);
        if (inventoryItem == null) {
            throw new IllegalArgumentException("No InventoryItem for id: " + id);
        }
        return inventoryItem;
    }

}

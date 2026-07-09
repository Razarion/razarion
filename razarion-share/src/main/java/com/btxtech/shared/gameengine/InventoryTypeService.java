package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.InventoryArtifact;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import jsinterop.annotations.JsType;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Beat
 * 25.10.2016.
 */
@Singleton
@JsType
public class InventoryTypeService {
    private final HashMap<Integer, InventoryItem> inventoryItems = new HashMap<>();
    private final HashMap<Integer, InventoryArtifact> inventoryArtifacts = new HashMap<>();

    @Inject
    public InventoryTypeService(InitializeService initializeService) {
        initializeService.receiveStaticGameConfig(this::init);
    }

    public void init(StaticGameConfig staticGameConfig) {
        setInventoryItems(staticGameConfig.getInventoryItems());
        setInventoryArtifacts(staticGameConfig.getInventoryArtifacts());
    }

    private void setInventoryItems(List<InventoryItem> inventoryItems) {
        this.inventoryItems.clear();
        if (inventoryItems != null) {
            for (InventoryItem inventoryItem : inventoryItems) {
                this.inventoryItems.put(inventoryItem.getId(), inventoryItem);
            }
        }
    }

    private void setInventoryArtifacts(List<InventoryArtifact> inventoryArtifacts) {
        this.inventoryArtifacts.clear();
        if (inventoryArtifacts != null) {
            for (InventoryArtifact inventoryArtifact : inventoryArtifacts) {
                this.inventoryArtifacts.put(inventoryArtifact.getId(), inventoryArtifact);
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

    public Collection<InventoryItem> getInventoryItems() {
        return inventoryItems.values();
    }

    public InventoryArtifact getInventoryArtifact(int id) {
        InventoryArtifact inventoryArtifact = inventoryArtifacts.get(id);
        if (inventoryArtifact == null) {
            throw new IllegalArgumentException("No InventoryArtifact for id: " + id);
        }
        return inventoryArtifact;
    }

    public Collection<InventoryArtifact> getInventoryArtifacts() {
        return inventoryArtifacts.values();
    }

}

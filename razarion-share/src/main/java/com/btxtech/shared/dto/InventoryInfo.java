package com.btxtech.shared.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 17.09.2017.
 */
public class InventoryInfo {
    private int crystals;
    private List<Integer> inventoryItemIds = new ArrayList<>();
    private List<Integer> inventoryArtifactIds = new ArrayList<>();

    public int getCrystals() {
        return crystals;
    }

    public InventoryInfo setCrystals(int crystals) {
        this.crystals = crystals;
        return this;
    }

    public List<Integer> getInventoryItemIds() {
        return inventoryItemIds;
    }

    public InventoryInfo setInventoryItemIds(List<Integer> inventoryItemIds) {
        this.inventoryItemIds = inventoryItemIds;
        return this;
    }

    public List<Integer> getInventoryArtifactIds() {
        return inventoryArtifactIds;
    }

    public InventoryInfo setInventoryArtifactIds(List<Integer> inventoryArtifactIds) {
        this.inventoryArtifactIds = inventoryArtifactIds;
        return this;
    }
}

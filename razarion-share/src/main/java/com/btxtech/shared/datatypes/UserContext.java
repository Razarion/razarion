package com.btxtech.shared.datatypes;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 30.08.2016.
 */
public class UserContext {
    private String name;
    private int levelId;
    private int xp;
    private int crystals;
    private List<Integer> inventoryItemIds = new ArrayList<>();
    private List<Integer> inventoryArtifactIds = new ArrayList<>();

    public String getName() {
        return name;
    }

    public UserContext setName(String name) {
        this.name = name;
        return this;
    }

    public int getLevelId() {
        return levelId;
    }

    public UserContext setLevelId(int levelId) {
        this.levelId = levelId;
        return this;
    }

    public int getXp() {
        return xp;
    }

    public UserContext setXp(int xp) {
        this.xp = xp;
        return this;
    }

    public List<Integer> getInventoryItemIds() {
        return inventoryItemIds;
    }

    public UserContext setInventoryItemIds(List<Integer> inventoryItemIds) {
        this.inventoryItemIds = inventoryItemIds;
        return this;
    }

    public void addInventoryItem(int inventoryItemId) {
        inventoryItemIds.add(inventoryItemId);
    }

    public List<Integer> getInventoryArtifactIds() {
        return inventoryArtifactIds;
    }

    public UserContext setInventoryArtifactIds(List<Integer> inventoryArtifactIds) {
        this.inventoryArtifactIds = inventoryArtifactIds;
        return this;
    }

    public int getCrystals() {
        return crystals;
    }

    public UserContext setCrystals(int crystals) {
        this.crystals = crystals;
        return this;
    }
}

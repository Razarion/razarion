package com.btxtech.shared.datatypes;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Beat
 * 30.08.2016.
 */
public class UserContext {
    private int userId;
    private String name;
    private boolean admin;
    private int levelId;
    private int xp;
    private int crystals;
    private List<Integer> inventoryItemIds = new ArrayList<>();
    private List<Integer> inventoryArtifactIds = new ArrayList<>();
    private Set<Integer> unlockedItemTypes = new HashSet<Integer>();
    private Set<Integer> unlockedQuests = new HashSet<Integer>();
    private Set<Integer> unlockedPlanets = new HashSet<Integer>();

    public int getUserId() {
        return userId;
    }

    public UserContext setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserContext setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isAdmin() {
        return admin;
    }

    public UserContext setAdmin(boolean admin) {
        this.admin = admin;
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

    public Set<Integer> getUnlockedItemTypes() {
        return unlockedItemTypes;
    }

    public UserContext setUnlockedItemTypes(Set<Integer> unlockedItemTypes) {
        this.unlockedItemTypes = unlockedItemTypes;
        return this;
    }

    public boolean containsUnlockedItemTypeId(int itemTypeId) {
        return unlockedItemTypes.contains(itemTypeId);
    }

    public Set<Integer> getUnlockedQuests() {
        return unlockedQuests;
    }

    public UserContext setUnlockedQuests(Set<Integer> unlockedQuests) {
        this.unlockedQuests = unlockedQuests;
        return this;
    }

    public Set<Integer> getUnlockedPlanets() {
        return unlockedPlanets;
    }

    public UserContext setUnlockedPlanets(Set<Integer> unlockedPlanets) {
        this.unlockedPlanets = unlockedPlanets;
        return this;
    }
}

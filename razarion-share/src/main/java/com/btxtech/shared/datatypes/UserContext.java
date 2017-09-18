package com.btxtech.shared.datatypes;


import java.util.HashSet;
import java.util.Set;

/**
 * Created by Beat
 * 30.08.2016.
 */
public class UserContext {
    private HumanPlayerId humanPlayerId;
    private String name; // May only in DB or unregistered user
    private boolean admin;
    private int levelId; // May only in DB or unregistered user
    private int xp; // May only in DB or unregistered user
    private Set<Integer> unlockedItemTypes = new HashSet<Integer>(); // May only in DB or unregistered user
    private Set<Integer> unlockedQuests = new HashSet<Integer>(); // May only in DB or unregistered user
    private Set<Integer> unlockedPlanets = new HashSet<Integer>(); // May only in DB or unregistered user

    public HumanPlayerId getHumanPlayerId() {
        return humanPlayerId;
    }

    public UserContext setHumanPlayerId(HumanPlayerId humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
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

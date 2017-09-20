package com.btxtech.shared.datatypes;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Beat
 * 30.08.2016.
 */
public class UserContext {
    private HumanPlayerId humanPlayerId;
    private String name;
    private boolean admin;
    private int levelId;
    private Map<Integer, Integer> unlockedItemLimit;
    private int xp;

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

    public Map<Integer, Integer> getUnlockedItemLimit() {
        return unlockedItemLimit;
    }

    public UserContext setUnlockedItemLimit(Map<Integer, Integer> unlockedItemLimit) {
        this.unlockedItemLimit = unlockedItemLimit;
        return this;
    }

    public int getXp() {
        return xp;
    }

    public UserContext setXp(int xp) {
        this.xp = xp;
        return this;
    }
}

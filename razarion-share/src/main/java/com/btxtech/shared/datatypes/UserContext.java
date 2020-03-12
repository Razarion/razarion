package com.btxtech.shared.datatypes;


import java.util.Map;

/**
 * Created by Beat
 * 30.08.2016.
 */
public class UserContext {
    private HumanPlayerId humanPlayerId;
    private String name;
    private boolean registered;
    private boolean admin;
    private boolean emailNotVerified;
    private Integer levelId;
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

    public boolean isRegistered() {
        return registered;
    }

    public UserContext setRegistered(boolean registered) {
        this.registered = registered;
        return this;
    }

    public boolean isAdmin() {
        return admin;
    }

    public UserContext setAdmin(boolean admin) {
        this.admin = admin;
        return this;
    }

    public boolean isEmailNotVerified() {
        return emailNotVerified;
    }

    public UserContext setEmailNotVerified(boolean emailNotVerified) {
        this.emailNotVerified = emailNotVerified;
        return this;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public UserContext setLevelId(Integer levelId) {
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

    public boolean checkName() {
        return name != null;
    }

    @Override
    public String toString() {
        return "UserContext{" +
                "humanPlayerId=" + humanPlayerId +
                ", name='" + name + '\'' +
                ", registered=" + registered +
                ", admin=" + admin +
                ", emailNotVerified=" + emailNotVerified +
                ", levelId=" + levelId +
                ", unlockedItemLimit=" + unlockedItemLimit +
                ", xp=" + xp +
                '}';
    }
}

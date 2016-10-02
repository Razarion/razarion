package com.btxtech.shared.datatypes;


/**
 * Created by Beat
 * 30.08.2016.
 */
public class UserContext {
    private String name;
    private int levelId;
    private int xp;

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
}

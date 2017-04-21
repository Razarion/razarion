package com.btxtech.server.user;

/**
 * Created by Beat
 * 21.02.2017.
 */
public class User {
    private long userId;
    private int levelId;
    private boolean admin;
    private String name;

    public User(long userId, int levelId, boolean admin) {
        this.userId = userId;
        this.levelId = levelId;
        this.admin = admin;
    }

    public long getUserId() {
        return userId;
    }

    public int getLevelId() {
        return levelId;
    }

    public boolean isAdmin() {
        return admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", levelId=" + levelId +
                ", admin=" + admin +
                '}';
    }
}

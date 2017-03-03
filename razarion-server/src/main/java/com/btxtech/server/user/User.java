package com.btxtech.server.user;

/**
 * Created by Beat
 * 21.02.2017.
 */
public class User {
    private long userId;
    private boolean admin;

    public User(long userId, boolean admin) {
        this.userId = userId;
        this.admin = admin;
    }

    public long getUserId() {
        return userId;
    }

    public boolean isAdmin() {
        return admin;
    }
}

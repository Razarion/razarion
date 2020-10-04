package com.btxtech.server.persistence.history;

/**
 * Created by Beat
 * on 11.01.2018.
 */
public class SimpleUserBackend {
    private int userId;
    private String name;

    public int getUserId() {
        return userId;
    }

    public SimpleUserBackend setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public String getName() {
        return name;
    }

    public SimpleUserBackend setName(String name) {
        this.name = name;
        return this;
    }
}

package com.btxtech.server.web;

import com.btxtech.server.user.User;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

/**
 * Created by Beat
 * 27.02.2017.
 */
@SessionScoped
public class Session implements Serializable {
    private String id;
    private User user;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id='" + id + '\'' +
                ", user=" + user +
                '}';
    }
}

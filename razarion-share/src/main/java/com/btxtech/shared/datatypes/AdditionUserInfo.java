package com.btxtech.shared.datatypes;

import java.util.Date;

/**
 * Created by Beat
 * on 12.03.2018.
 */
public class AdditionUserInfo {
    private String userId;
    private Date lastLoggedIn;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(Date lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }
}

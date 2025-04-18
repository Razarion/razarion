package com.btxtech.server.user;

import com.btxtech.shared.datatypes.UserContext;

import java.util.Date;

public class PlayerSession {
    private final String httpSessionId;
    private final Date time = new Date();
    private UserContext userContext;

    public PlayerSession(String httpSessionId) {
        this.httpSessionId = httpSessionId;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }

    public Date getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "SessionHolder{" +
                "id='" + httpSessionId + '\'' +
                ", userContext=" + userContext +
                ", time=" + time +
                '}';
    }

}

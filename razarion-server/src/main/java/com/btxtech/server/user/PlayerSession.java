package com.btxtech.server.user;

import com.btxtech.shared.datatypes.UserContext;

import java.util.Date;
import java.util.Locale;

public class PlayerSession {
    private String httpSessionId;
    private UserContext userContext;
    private Locale locale;
    private Date time = new Date();

    public PlayerSession(String httpSessionId, Locale locale) {
        this.httpSessionId = httpSessionId;
        this.locale = locale;
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

    public Locale getLocale() {
        return locale;
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

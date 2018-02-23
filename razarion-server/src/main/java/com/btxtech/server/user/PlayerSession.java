package com.btxtech.server.user;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;

import java.util.Date;
import java.util.Locale;

/**
 * Created by Beat
 * 23.04.2017.
 */
public class PlayerSession {
    private String httpSessionId;
    private UserContext userContext;
    private Locale locale;
    private UnregisteredUser unregisteredUser;
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

    public UnregisteredUser getUnregisteredUser() {
        return unregisteredUser;
    }

    public void setUnregisteredUser(UnregisteredUser unregisteredUser) {
        this.unregisteredUser = unregisteredUser;
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

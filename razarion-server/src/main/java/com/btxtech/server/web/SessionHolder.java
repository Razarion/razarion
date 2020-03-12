package com.btxtech.server.web;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.shared.datatypes.UserContext;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

/**
 * Created by Beat
 * 27.02.2017.
 */
@SessionScoped
public class SessionHolder implements Serializable {
    private PlayerSession playerSession;

    public PlayerSession getPlayerSession() {
        return playerSession;
    }

    public void setPlayerSession(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    public boolean isLoggedIn() {
        UserContext userContext = playerSession.getUserContext();
        return userContext != null && userContext.isRegistered();
    }
}

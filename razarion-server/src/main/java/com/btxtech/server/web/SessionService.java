package com.btxtech.server.web;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Beat
 * 23.04.2017.
 */
@Singleton
public class SessionService {
    private final Map<String, PlayerSession> sessions = new HashMap<>();

    public PlayerSession sessionCreated(String httpSessionId, Locale locale) {
        PlayerSession playerSession = new PlayerSession(httpSessionId, locale);
        synchronized (sessions) {
            sessions.put(httpSessionId, playerSession);
        }
        return playerSession;
    }

    public void sessionDestroyed(String sessionId) {
        synchronized (sessions) {
            sessions.remove(sessionId);
        }
    }

    public PlayerSession getSession(String sessionId) {
        PlayerSession playerSession;
        synchronized (sessions) {
            playerSession = sessions.get(sessionId);
        }
        if (playerSession == null) {
            throw new IllegalArgumentException("No playerSession for id: " + sessionId);
        }
        return playerSession;
    }

    public PlayerSession findPlayerSession(HumanPlayerId humanPlayerId) {
        synchronized (sessions) {
            for (PlayerSession playerSession : sessions.values()) {
                if (playerSession.getUserContext() != null && playerSession.getUserContext().getHumanPlayerId() != null && playerSession.getUserContext().getHumanPlayerId().equals(humanPlayerId)) {
                    return playerSession;
                }
            }
        }
        return null;
    }

    public void updateUserContext(HumanPlayerId humanPlayerId, UserContext userContext) {
        synchronized (sessions) {
            for (PlayerSession playerSession : sessions.values()) {
                if (playerSession.getUserContext() != null && playerSession.getUserContext().getHumanPlayerId() != null && playerSession.getUserContext().getHumanPlayerId().equals(humanPlayerId)) {
                    playerSession.setUserContext(userContext);
                }
            }
        }
    }
}

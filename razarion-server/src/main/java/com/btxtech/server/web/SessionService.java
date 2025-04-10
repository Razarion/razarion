package com.btxtech.server.web;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.shared.datatypes.UserContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SessionService {
    private final Map<String, PlayerSession> sessions = new HashMap<>();


    public PlayerSession getSession(String sessionId) {
        PlayerSession playerSession;
        synchronized (sessions) {
            playerSession = sessions.get(sessionId);
        }
        if (playerSession == null) {
            playerSession = createSession(sessionId);
        }
        return playerSession;
    }

    public PlayerSession sessionDestroyed(String sessionId) {
        synchronized (sessions) {
            return sessions.remove(sessionId);
        }
    }


    private PlayerSession createSession(String httpSessionId) {
        PlayerSession playerSession = new PlayerSession(httpSessionId);
        synchronized (sessions) {
            sessions.put(httpSessionId, playerSession);
        }
        return playerSession;
    }

    public boolean checkSession(String sessionId) {
        synchronized (sessions) {
            return sessions.containsKey(sessionId);
        }
    }

    public PlayerSession findPlayerSession(int userId) {
        synchronized (sessions) {
            for (PlayerSession playerSession : sessions.values()) {
                if (playerSession.getUserContext() != null && playerSession.getUserContext().getUserId() == userId) {
                    return playerSession;
                }
            }
        }
        return null;
    }

    public void updateUserContext(int userId, UserContext userContext) {
        synchronized (sessions) {
            for (PlayerSession playerSession : sessions.values()) {
                if (playerSession.getUserContext() != null && playerSession.getUserContext().getUserId() == userId) {
                    playerSession.setUserContext(userContext);
                }
            }
        }
    }
}

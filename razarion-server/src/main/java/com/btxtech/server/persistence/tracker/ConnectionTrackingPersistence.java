package com.btxtech.server.persistence.tracker;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Date;

/**
 * Created by Beat
 * on 01.01.2018.
 */
@Singleton
public class ConnectionTrackingPersistence {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Transactional
    public void onSystemConnectionOpened(String sessionId, PlayerSession playerSession) {
        try {
            persist(ConnectionTrackerEntity.Type.SYSTEM_OPEN, sessionId, playerSession.getUserContext().getHumanPlayerId().getPlayerId());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Transactional
    public void onSystemConnectionClosed(String sessionId, PlayerSession playerSession) {
        try {
            persist(ConnectionTrackerEntity.Type.SYSTEM_CLOSE, sessionId, playerSession.getUserContext().getHumanPlayerId().getPlayerId());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Transactional
    public void onGameConnectionOpened(String sessionId, HumanPlayerId humanPlayerId) {
        try {
            persist(ConnectionTrackerEntity.Type.GAME_OPEN, sessionId, humanPlayerId.getPlayerId());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Transactional
    public void onGameConnectionClosed(String sessionId, HumanPlayerId humanPlayerId) {
        try {
            persist(ConnectionTrackerEntity.Type.GAME_CLOSE, sessionId, humanPlayerId.getPlayerId());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private void persist(ConnectionTrackerEntity.Type type, String sessionId, int humanPlayerId) {
        ConnectionTrackerEntity connectionTrackerEntity = new ConnectionTrackerEntity();
        connectionTrackerEntity.setTimeStamp(new Date());
        connectionTrackerEntity.setType(type);
        connectionTrackerEntity.setSessionId(sessionId);
        connectionTrackerEntity.setHumanPlayerId(humanPlayerId);
        entityManager.persist(connectionTrackerEntity);
    }

}

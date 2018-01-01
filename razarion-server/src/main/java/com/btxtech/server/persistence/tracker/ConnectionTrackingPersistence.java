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
            ConnectionTrackerEntity connectionTrackerEntity = new ConnectionTrackerEntity();
            connectionTrackerEntity.setSystemOpen(new Date());
            connectionTrackerEntity.setSessionId(sessionId);
            connectionTrackerEntity.setHumanPlayerId(playerSession.getUserContext().getHumanPlayerId().getPlayerId());
            entityManager.persist(connectionTrackerEntity);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Transactional
    public void onSystemConnectionClosed(String sessionId, PlayerSession playerSession) {
        try {
            ConnectionTrackerEntity connectionTrackerEntity = new ConnectionTrackerEntity();
            connectionTrackerEntity.setSystemClose(new Date());
            connectionTrackerEntity.setSessionId(sessionId);
            connectionTrackerEntity.setHumanPlayerId(playerSession.getUserContext().getHumanPlayerId().getPlayerId());
            entityManager.persist(connectionTrackerEntity);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Transactional
    public void onGameConnectionOpened(String sessionId, HumanPlayerId humanPlayerId) {
        try {
            ConnectionTrackerEntity connectionTrackerEntity = new ConnectionTrackerEntity();
            connectionTrackerEntity.setGameOpen(new Date());
            connectionTrackerEntity.setSessionId(sessionId);
            connectionTrackerEntity.setHumanPlayerId(humanPlayerId.getPlayerId());
            entityManager.persist(connectionTrackerEntity);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Transactional
    public void onGameConnectionClosed(String sessionId, HumanPlayerId humanPlayerId) {
        try {
            ConnectionTrackerEntity connectionTrackerEntity = new ConnectionTrackerEntity();
            connectionTrackerEntity.setGameClose(new Date());
            connectionTrackerEntity.setSessionId(sessionId);
            connectionTrackerEntity.setHumanPlayerId(humanPlayerId.getPlayerId());
            entityManager.persist(connectionTrackerEntity);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }
}

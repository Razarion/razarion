package com.btxtech.server.persistence;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.debugtool.DebugHelper;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Date;

/**
 * Created by Beat
 * on 13.02.2018.
 */
@Singleton
public class ServerDebugHelper implements DebugHelper {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    @Transactional
    public void debugToDb(String debugMessage) {
        debugToDb(debugMessage, null, "Server");
    }

    @Transactional
    public void debugToDb(String debugMessage, String sessionId, String system) {
        try {
            DebugEntity debugEntity = new DebugEntity();
            debugEntity.setDebugMessage(debugMessage);
            debugEntity.setSessionId(sessionId);
            debugEntity.setSystem(system);
            debugEntity.setTimeStamp(new Date());
            entityManager.persist(debugEntity);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }
}

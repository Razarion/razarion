package com.btxtech.server.rest;

import com.btxtech.server.persistence.ClipPersistence;
import com.btxtech.shared.dto.ClipConfig;
import com.btxtech.shared.rest.ClipProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 15.10.2016.
 */
public class ClipProviderImpl implements ClipProvider {
    @Inject
    private ClipPersistence clipPersistence;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    public ClipConfig create() {
        try {
            return clipPersistence.create();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public List<ClipConfig> read() {
        try {
            return clipPersistence.readClipConfigs();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void update(ClipConfig clipConfig) {
        try {
            clipPersistence.update(clipConfig);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void delete(int id) {
        try {
            clipPersistence.delete(id);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}

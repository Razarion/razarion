package com.btxtech.server.rest;

import com.btxtech.server.persistence.server.ServerGameEnginePersistence;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.rest.ServerGameEngineEditorProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * on 28.07.2017.
 */
public class ServerGameEngineEditorProviderImpl implements ServerGameEngineEditorProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ServerGameEnginePersistence serverGameEnginePersistence;

    @Override
    public List<ObjectNameId> readStartRegionObjectNameIds() {
        try {
            return serverGameEnginePersistence.readStartRegionObjectNameIds();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public StartRegionConfig readStartRegionConfig(int id) {
        try {
            return serverGameEnginePersistence.readStartRegionConfig(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public StartRegionConfig createStartRegionConfig() {
        try {
            return serverGameEnginePersistence.createStartRegionConfig();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateStartRegionConfig(StartRegionConfig startRegionConfig) {
        try {
            serverGameEnginePersistence.updateStartRegionConfig(startRegionConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteStartRegionConfig(int id) {
        try {
            serverGameEnginePersistence.deleteStartRegion(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}

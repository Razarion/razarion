package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.server.ServerGameEngineConfigEntity;
import com.btxtech.server.persistence.server.ServerGameEngineCrudPersistence;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.rest.ServerGameEngineEditorController;

import javax.inject.Inject;

public class ServerGameEngineEditorControllerImpl extends AbstractCrudController<ServerGameEngineConfig, ServerGameEngineConfigEntity> implements ServerGameEngineEditorController {
    @Inject
    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;

    @Override
    protected AbstractCrudPersistence<ServerGameEngineConfig, ServerGameEngineConfigEntity> getCrudPersistence() {
        return serverGameEngineCrudPersistence;
    }
}

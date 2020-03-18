package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.GameUiContextCrudPersistence;
import com.btxtech.server.persistence.GameUiControlContextEntity;
import com.btxtech.shared.dto.GameUiContextConfig;
import com.btxtech.shared.rest.GameUiContextEditorController;

import javax.inject.Inject;

public class GameUiContextEditorControllerImpl extends AbstractCrudController<GameUiContextConfig, GameUiControlContextEntity> implements GameUiContextEditorController {
    @Inject
    private GameUiContextCrudPersistence gameUiContextCrudPersistence;

    @Override
    protected AbstractCrudPersistence<GameUiContextConfig, GameUiControlContextEntity> getCrudPersistence() {
        return gameUiContextCrudPersistence;
    }
}

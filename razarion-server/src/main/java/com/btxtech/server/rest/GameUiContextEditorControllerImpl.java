package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractConfigCrudPersistence;
import com.btxtech.server.persistence.GameUiContextCrudPersistence;
import com.btxtech.server.persistence.GameUiContextEntity;
import com.btxtech.shared.dto.GameUiContextConfig;
import com.btxtech.shared.rest.GameUiContextEditorController;

import javax.inject.Inject;

public class GameUiContextEditorControllerImpl extends AbstractCrudController<GameUiContextConfig, GameUiContextEntity> implements GameUiContextEditorController {
    @Inject
    private GameUiContextCrudPersistence gameUiContextCrudPersistence;

    @Override
    protected AbstractConfigCrudPersistence<GameUiContextConfig, GameUiContextEntity> getCrudPersistence() {
        return gameUiContextCrudPersistence;
    }
}

package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractConfigCrudPersistence;
import com.btxtech.server.persistence.BoxItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.BoxItemTypeEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.rest.BoxItemTypeEditorController;

import javax.inject.Inject;

/**
 * Created by Beat
 * 20.11.2015.
 */
public class BoxItemTypeEditorControllerImpl extends AbstractCrudController<BoxItemType, BoxItemTypeEntity> implements BoxItemTypeEditorController {
    @Inject
    private BoxItemTypeCrudPersistence persistenceService;

    @Override
    protected AbstractConfigCrudPersistence<BoxItemType, BoxItemTypeEntity> getCrudPersistence() {
        return persistenceService;
    }
}

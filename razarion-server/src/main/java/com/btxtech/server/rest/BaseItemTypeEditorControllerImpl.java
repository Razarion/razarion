package com.btxtech.server.rest;

import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.rest.BaseItemTypeEditorController;

import javax.inject.Inject;

/**
 * Created by Beat
 * 20.11.2015.
 */
public class BaseItemTypeEditorControllerImpl extends AbstractCrudController<BaseItemType, BaseItemTypeEntity> implements BaseItemTypeEditorController {
    @Inject
    private BaseItemTypeCrudPersistence persistenceService;

    @Override
    protected BaseItemTypeCrudPersistence getCrudPersistence() {
        return persistenceService;
    }
}

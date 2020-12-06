package com.btxtech.server.rest;

import com.btxtech.server.persistence.itemtype.ResourceItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.rest.ResourceItemTypeEditorController;

import javax.inject.Inject;

/**
 * Created by Beat
 * 20.11.2015.
 */
public class ResourceItemTypeEditorControllerImpl extends AbstractCrudController<ResourceItemType, ResourceItemTypeEntity> implements ResourceItemTypeEditorController {
    @Inject
    private ResourceItemTypeCrudPersistence persistenceService;

    @Override
    protected ResourceItemTypeCrudPersistence getCrudPersistence() {
        return persistenceService;
    }
}

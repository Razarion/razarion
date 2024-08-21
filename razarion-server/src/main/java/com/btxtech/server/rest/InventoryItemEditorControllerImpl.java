package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractConfigCrudPersistence;
import com.btxtech.server.persistence.inventory.InventoryItemEntity;
import com.btxtech.server.persistence.inventory.InventoryItemCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.rest.InventoryItemEditorController;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 19.09.2017.
 */
public class InventoryItemEditorControllerImpl extends AbstractCrudController<InventoryItem, InventoryItemEntity> implements InventoryItemEditorController {
    @Inject
    private InventoryItemCrudPersistence inventoryPersistence;

    @Override
    protected AbstractConfigCrudPersistence<InventoryItem, InventoryItemEntity> getCrudPersistence() {
        return inventoryPersistence;
    }
}

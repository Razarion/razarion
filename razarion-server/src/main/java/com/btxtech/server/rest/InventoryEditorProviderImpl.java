package com.btxtech.server.rest;

import com.btxtech.server.persistence.inventory.InventoryPersistence;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.rest.InventoryEditorProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * on 19.09.2017.
 */
public class InventoryEditorProviderImpl implements InventoryEditorProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private InventoryPersistence inventoryPersistence;

    @Override
    public List<ObjectNameId> readInventoryItemObjectNameIds() {
        try {
            return inventoryPersistence.readInventoryItemObjectNameIds();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public InventoryItem createInventoryItem() {
        try {
            return inventoryPersistence.createInventoryItem();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteInventoryItem(int id) {
        try {
            inventoryPersistence.deleteInventoryItem(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateInventoryItem(InventoryItem inventoryItem) {
        try {
            inventoryPersistence.updateInventoryItem(inventoryItem);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public InventoryItem readInventoryItem(int id) {
        try {
            return inventoryPersistence.readInventoryItem(id);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}

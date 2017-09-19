package com.btxtech.client.editor.inventory;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.rest.InventoryEditorProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 24.08.2016.
 */
@ApplicationScoped
public class InventoryItemCrud extends AbstractCrudeEditor<InventoryItem> {
    private Logger logger = Logger.getLogger(InventoryItemCrud.class.getName());
    @Inject
    private Caller<InventoryEditorProvider> provider;
    private List<ObjectNameId> objectNameIds = new ArrayList<>();

    @Override
    public void init() {
        provider.call((RemoteCallback<List<ObjectNameId>>) objectNameIds -> {
            InventoryItemCrud.this.objectNameIds = objectNameIds;
            fire();
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "InventoryEditorProvider.readInventoryItemObjectNameIds failed: " + message, throwable);
            return false;
        }).readInventoryItemObjectNameIds();
    }


    @Override
    public void create() {
        provider.call((RemoteCallback<InventoryItem>) inventoryItem -> {
            objectNameIds.add(inventoryItem.createObjectNameId());
            fire();
            fireSelection(inventoryItem.createObjectNameId());
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "InventoryEditorProvider.createInventoryItem failed: " + message, throwable);
            return false;
        }).createInventoryItem();
    }

    @Override
    public void delete(InventoryItem inventoryItem) {
        provider.call(ignore -> {
            objectNameIds.removeIf(objectNameId -> objectNameId.getId() == inventoryItem.getId());
            fire();
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "InventoryEditorProvider.deleteInventoryItem failed: " + message, throwable);
            return false;
        }).deleteInventoryItem(inventoryItem.getId());
    }

    @Override
    public void save(InventoryItem inventoryItem) {
        provider.call(ignore -> fire(), (message, throwable) -> {
            logger.log(Level.SEVERE, "InventoryEditorProvider.updateInventoryItem failed: " + message, throwable);
            return false;
        }).updateInventoryItem(inventoryItem);
    }

    @Override
    public void reload() {
        init();
    }

    @Override
    public void getInstance(ObjectNameId objectNameId, Consumer<InventoryItem> callback) {
        provider.call((RemoteCallback<InventoryItem>) callback::accept, (message, throwable) -> {
            logger.log(Level.SEVERE, "InventoryEditorProvider.readInventoryItem failed: " + message, throwable);
            return false;
        }).readInventoryItem(objectNameId.getId());
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return objectNameIds;
    }
}

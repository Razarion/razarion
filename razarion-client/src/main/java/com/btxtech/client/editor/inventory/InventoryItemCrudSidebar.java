package com.btxtech.client.editor.inventory;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.rest.ServerGameEngineControlProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 24.08.2016.
 */
@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class InventoryItemCrudSidebar extends AbstractCrudeParentSidebar<InventoryItem, InventoryItemPropertyPanel> {
    private Logger logger = Logger.getLogger(InventoryItemCrudSidebar.class.getName());
    @Inject
    private InventoryItemCrud inventoryItemCrud;
    @Inject
    private Instance<InventoryItemPropertyPanel> propertyPanelInstance;
    @Inject
    private Caller<ServerGameEngineControlProvider> provider;

    @Override
    public void onConfigureDialog() {
        super.onConfigureDialog();
        getSideBarPanel().addButton("Restart", () -> provider.call(ignore -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Calling ServerGameEngineControlProvider.reloadStatic() failed: " + message, throwable);
            return false;
        }).reloadStatic());
    }

    @Override
    protected CrudEditor<InventoryItem> getCrudEditor() {
        return inventoryItemCrud;
    }

    @Override
    protected InventoryItemPropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }
}

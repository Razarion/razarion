package com.btxtech.client.editor.inventory;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 24.08.2016.
 */
@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class InventoryItemCrudSidebar extends AbstractCrudeParentSidebar<InventoryItem, InventoryItemPropertyPanel> {
    // private Logger logger = Logger.getLogger(InventoryItemCrudSidebar.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private InventoryItemCrud inventoryItemCrud;
    @Inject
    private Instance<InventoryItemPropertyPanel> propertyPanelInstance;

    @Override
    public void onConfigureDialog() {
        super.onConfigureDialog();
//  TODO      getEditorPanel().addButton("Restart", () -> provider.call(ignore -> {
//        }, exceptionHandler.restErrorHandler("Calling ServerGameEngineControlProvider.reloadStatic() failed: ")).reloadStatic());
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

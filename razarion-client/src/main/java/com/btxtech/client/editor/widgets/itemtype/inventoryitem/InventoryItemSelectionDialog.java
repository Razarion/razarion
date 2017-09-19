package com.btxtech.client.editor.widgets.itemtype.inventoryitem;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 22.08.2016.
 */
@Templated("InventoryItemSelectionDialog.html#inventory-item-selection-dialog")
public class InventoryItemSelectionDialog extends Composite implements ModalDialogContent<Integer> {
    @Inject
    private InventoryTypeService inventoryTypeService;
    @Inject
    @AutoBound
    private DataBinder<List<InventoryItem>> binder;
    @Inject
    @Bound
    @DataField
    @ListContainer("tbody")
    private ListComponent<InventoryItem, InventoryItemEntry> inventoryItems;
    private ModalDialogPanel<Integer> modalDialogPanel;

    @Override
    public void init(Integer selectedId) {
        DOMUtil.removeAllElementChildren(inventoryItems.getElement()); // Remove placeholder table row from template.
        inventoryItems.addComponentCreationHandler(inventoryItemEntry -> inventoryItemEntry.setInventoryItemSelectionDialog(InventoryItemSelectionDialog.this));
        binder.setModel(new ArrayList<>(inventoryTypeService.getInventoryItems()));
        inventoryItems.setSelector(inventoryItemEntry -> inventoryItemEntry.setSelected(true));
        inventoryItems.setDeselector(inventoryItemEntry -> inventoryItemEntry.setSelected(false));
        if (selectedId != null) {
            // Problem whit Errai binder proxy end equals
            inventoryItems.selectModel(BindableProxyFactory.getBindableProxy(inventoryTypeService.getInventoryItem(selectedId)));
        }
    }

    @Override
    public void customize(ModalDialogPanel<Integer> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    public void selectComponent(InventoryItemEntry widget) {
        inventoryItems.deselectAll();
        inventoryItems.selectComponent(widget);
        modalDialogPanel.setApplyValue(widget.getValue().getId());
    }

    @Override
    public void onClose() {

    }
}

package com.btxtech.client.editor.widgets.itemtype.inventoryitem;

import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 22.08.2016.
 */

@Templated("InventoryItemSelectionDialog.html#tableRow")
public class InventoryItemEntry implements TakesValue<InventoryItem>, IsElement {
    @Inject
    @DataField
    private TableRow tableRow;
    @Inject
    @DataField
    private Label inventoryItemId;
    @Inject
    @DataField
    private Label inventoryItemName;
    private InventoryItem inventoryItem;
    private InventoryItemSelectionDialog inventoryItemSelectionDialog;

    @Override
    public HTMLElement getElement() {
        return tableRow;
    }

    @Override
    public void setValue(InventoryItem inventoryItem) {
        this.inventoryItem = inventoryItem;
        inventoryItemId.setText(DisplayUtils.handleInteger(inventoryItem.getId()));
        inventoryItemName.setText(inventoryItem.getInternalName());
    }

    @Override
    public InventoryItem getValue() {
        return inventoryItem;
    }

    @EventHandler("tableRow")
    public void onClick(ClickEvent event) {
        inventoryItemSelectionDialog.selectComponent(this);
    }

    public void setSelected(boolean selected) {
        if (selected) {
            DOMUtil.addCSSClass(tableRow, "generic-gallery-table-row-selected");
            DOMUtil.removeCSSClass(tableRow, "generic-gallery-table-row-not-selected");
        } else {
            DOMUtil.addCSSClass(tableRow, "generic-gallery-table-row-not-selected");
            DOMUtil.removeCSSClass(tableRow, "generic-gallery-table-row-selected");
        }
    }

    public void setInventoryItemSelectionDialog(InventoryItemSelectionDialog inventoryItemSelectionDialog) {
        this.inventoryItemSelectionDialog = inventoryItemSelectionDialog;
    }
}

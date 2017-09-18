package com.btxtech.client.dialog.boxcontent;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 26.10.2016.
 */
@Templated("BoxContentDialog.html#box-picked-dialog")
public class BoxContentDialog extends Composite implements ModalDialogContent<BoxContent> {
    @Inject
    @DataField
    private Label crystalLabel;
    @Inject
    @DataField
    @ListContainer("div")
    private ListComponent<InventoryItem, InventoryItemComponent> inventoryItemTable;

    @Override
    public void init(BoxContent boxContent) {
        crystalLabel.setText(I18nHelper.getConstants().crystalAmount(boxContent.getCrystals()));
        DOMUtil.removeAllElementChildren(inventoryItemTable.getElement()); // Remove placeholder table row from template.
        inventoryItemTable.setValue(boxContent.getInventoryItems());
    }

    @Override
    public void customize(ModalDialogPanel<BoxContent> modalDialogPanel) {

    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void onClose() {
        // Ignore
    }

}

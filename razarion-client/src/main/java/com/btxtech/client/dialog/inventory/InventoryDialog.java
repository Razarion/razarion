package com.btxtech.client.dialog.inventory;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.uiservice.cockpit.CockpitService;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.inventory.InventoryItemModel;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.tip.GameTipService;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 29.10.2016.
 */
@Templated("InventoryDialog.html#inventory-dialog")
public class InventoryDialog extends Composite implements ModalDialogContent<Void> {
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private GameTipService gameTipService;
    @Inject
    private InventoryUiService inventoryUiService;
    @Inject
    private CockpitService cockpitService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label crystalsLabel;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    @ListContainer("div")
    private ListComponent<InventoryItemModel, InventoryItemWidget> inventoryItemTable;
    private ModalDialogPanel<Void> modalDialogPanel;

    @Override
    public void init(Void aVoid) {
        UserContext userContext = gameUiControl.getUserContext();
        crystalsLabel.setText(I18nHelper.getConstants().crystalAmount(userContext.getCrystals()));

        DOMUtil.removeAllElementChildren(inventoryItemTable.getElement()); // Remove placeholder table row from template.
        inventoryItemTable.addComponentCreationHandler(inventoryItemWidget -> inventoryItemWidget.setInventoryDialog(this));
        inventoryItemTable.setValue(inventoryUiService.gatherInventoryItemModels(userContext));
        cockpitService.onInventoryDialogOpened(inventoryItemId -> {
            for (InventoryItemModel inventoryItemModel : inventoryItemTable.getValue()) {
                if (inventoryItemModel.getInventoryItem().getId() == (int) inventoryItemId) {
                    return inventoryItemTable.getComponent(inventoryItemModel).orElseThrow(IllegalStateException::new).getInventoryUseButtonLocation();
                }
            }
            throw new IllegalStateException("No inventory item id: " + inventoryItemId);
        });
    }

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    @Override
    public void onClose() {
        gameTipService.onInventoryDialogClosed();
        cockpitService.onInventoryDialogClosed();
    }

    public void close() {
        modalDialogPanel.close();
    }
}

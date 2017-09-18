package com.btxtech.client.dialog.inventory;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.uiservice.cockpit.CockpitService;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.inventory.InventoryItemModel;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.tip.GameTipService;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 29.10.2016.
 */
@Templated("InventoryDialog.html#inventory-dialog")
public class InventoryDialog extends Composite implements ModalDialogContent<Void> {
    @Inject
    private InventoryTypeService inventoryTypeService;
    @Inject
    private GameTipService gameTipService;
    @Inject
    private InventoryUiService inventoryUiService;
    @Inject
    private CockpitService cockpitService;
    @Inject
    @DataField
    private Label crystalsLabel;
    @Inject
    @DataField
    @ListContainer("div")
    private ListComponent<InventoryItemModel, InventoryItemWidget> inventoryItemTable;
    private ModalDialogPanel<Void> modalDialogPanel;

    @Override
    public void init(Void aVoid) {
        DOMUtil.removeAllElementChildren(inventoryItemTable.getElement()); // Remove placeholder table row from template.
        inventoryItemTable.addComponentCreationHandler(inventoryItemWidget -> inventoryItemWidget.setInventoryDialog(this));

        inventoryUiService.provideInventoryInfo(inventoryInfo -> {
            crystalsLabel.setText(I18nHelper.getConstants().crystalAmount(inventoryInfo.getCrystals()));
            inventoryItemTable.setValue(sumUpInventoryItemModels(inventoryInfo.getInventoryItemIds()));
            cockpitService.onInventoryDialogOpened(inventoryItemId -> {
                for (InventoryItemModel inventoryItemModel : inventoryItemTable.getValue()) {
                    if (inventoryItemModel.getInventoryItem().getId() == inventoryItemId) {
                        return inventoryItemTable.getComponent(inventoryItemModel).orElseThrow(IllegalStateException::new).getInventoryUseButtonLocation();
                    }
                }
                throw new IllegalStateException("No inventory item id: " + inventoryItemId);
            });
        });
    }

    private List<InventoryItemModel> sumUpInventoryItemModels(List<Integer> inventoryItemIds) {
        if (inventoryItemIds != null) {
            Map<Integer, InventoryItemModel> inventoryItemModels = new HashMap<>();
            for (Integer inventoryItemId : inventoryItemIds) {
                InventoryItemModel model = inventoryItemModels.computeIfAbsent(inventoryItemId, k -> new InventoryItemModel(inventoryTypeService.getInventoryItem(inventoryItemId)));
                model.increaseItemCount();
            }
            return new ArrayList<>(inventoryItemModels.values());
        } else {
            return Collections.emptyList();
        }
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

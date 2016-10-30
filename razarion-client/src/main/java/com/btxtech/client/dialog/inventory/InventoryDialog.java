package com.btxtech.client.dialog.inventory;

import com.btxtech.client.clientI18n.ClientI18nHelper;
import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.InventoryService;
import com.btxtech.shared.gameengine.datatypes.InventoryItemModel;
import com.btxtech.uiservice.storyboard.StoryboardService;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * Created by Beat
 * 29.10.2016.
 */
@Templated("InventoryDialog.html#inventory-dialog")
public class InventoryDialog extends Composite implements ModalDialogContent<Void> {
    @Inject
    private StoryboardService storyboardService;
    @Inject
    private InventoryService inventoryService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label crystalsLabel;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    @ListContainer("div")
    private ListComponent<InventoryItemModel, InventoryItemWidget> inventoryItemTable;

    @Override
    public void init(Void aVoid) {
        UserContext userContext = storyboardService.getUserContext();
        crystalsLabel.setText(ClientI18nHelper.CONSTANTS.crystalAmount(userContext.getCrystals()));

        DOMUtil.removeAllElementChildren(inventoryItemTable.getElement()); // Remove placeholder table row from template.
        inventoryItemTable.setValue(new ArrayList<>(inventoryService.gatherInventoryItemModels(userContext)));
    }

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {

    }

    @Override
    public void onClose() {

    }
}

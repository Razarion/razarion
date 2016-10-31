package com.btxtech.client.dialog.inventory;

import com.btxtech.client.clientI18n.ClientI18nHelper;
import com.btxtech.uiservice.inventory.InventoryItemModel;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Beat
 * 29.10.2016.
 */
@Templated("InventoryDialog.html#inventoryItem")
public class InventoryItemWidget implements TakesValue<InventoryItemModel>, IsElement {
    @Inject
    private InventoryUiService inventoryUiService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    @Named("div")
    private Div inventoryItem;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label inventoryItemName;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Image inventoryItemImage;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label inventoryItemCount;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button inventoryItemUseButton;
    private InventoryItemModel inventoryItemModel;
    private InventoryDialog inventoryDialog;

    @Override
    public void setValue(InventoryItemModel inventoryItemModel) {
        this.inventoryItemModel = inventoryItemModel;
        inventoryItemName.setText(inventoryItemModel.getInventoryItem().getName());
        inventoryItemImage.setUrl(RestUrl.getImageServiceUrlSafe(inventoryItemModel.getInventoryItem().getImageId()));
        inventoryItemCount.setText(ClientI18nHelper.CONSTANTS.youOwn(inventoryItemModel.getItemCount()));
    }

    @Override
    public InventoryItemModel getValue() {
        return inventoryItemModel;
    }

    @Override
    public HTMLElement getElement() {
        return inventoryItem;
    }

    @EventHandler("inventoryItemUseButton")
    private void onInventoryItemUseButtonClicked(ClickEvent event) {
        inventoryDialog.close();
        inventoryUiService.useItem(inventoryItemModel.getInventoryItem());
    }

    public void setInventoryDialog(InventoryDialog inventoryDialog) {
        this.inventoryDialog = inventoryDialog;
    }
}

package com.btxtech.client.dialog.inventory;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.inventory.InventoryItemModel;
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
    @Inject
    @DataField
    @Named("div")
    private Div inventoryItem;
    @Inject
    @DataField
    private Label inventoryItemName;
    @Inject
    @DataField
    private Image inventoryItemImage;
    @Inject
    @DataField
    private Label inventoryItemCount;
    @Inject
    @DataField
    private Button inventoryItemUseButton;
    private InventoryItemModel inventoryItemModel;
    private InventoryDialog inventoryDialog;

    @Override
    public void setValue(InventoryItemModel inventoryItemModel) {
        this.inventoryItemModel = inventoryItemModel;
        inventoryItemName.setText(I18nHelper.getLocalizedString(inventoryItemModel.getInventoryItem().getI18nName()));
        inventoryItemImage.setUrl(RestUrl.getImageServiceUrlSafe(inventoryItemModel.getInventoryItem().getImageId()));
        inventoryItemCount.setText(I18nHelper.getConstants().youOwn(inventoryItemModel.getItemCount()));
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
        inventoryUiService.useItem(inventoryItemModel.getInventoryItem());
        inventoryDialog.close();
    }

    void setInventoryDialog(InventoryDialog inventoryDialog) {
        this.inventoryDialog = inventoryDialog;
    }

    Rectangle getInventoryUseButtonLocation() {
        return new Rectangle(inventoryItemUseButton.getAbsoluteLeft(), inventoryItemUseButton.getAbsoluteTop(), inventoryItemUseButton.getOffsetWidth(), inventoryItemUseButton.getOffsetHeight());
    }
}
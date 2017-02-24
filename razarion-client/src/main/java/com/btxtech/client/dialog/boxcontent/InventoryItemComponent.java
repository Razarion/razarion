package com.btxtech.client.dialog.boxcontent;

import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.rest.RestUrl;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Beat
 * 24.02.2017.
 */
@Templated("BoxContentDialog.html#inventoryItem")
public class InventoryItemComponent implements TakesValue<InventoryItem>, IsElement {
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
    private InventoryItem inventoryItemValue;

    @Override
    public void setValue(InventoryItem value) {
        inventoryItemValue = value;
        inventoryItemName.setText(value.getName());
        inventoryItemImage.setUrl(RestUrl.getImageServiceUrlSafe(value.getImageId()));
    }

    @Override
    public InventoryItem getValue() {
        return inventoryItemValue;
    }

    @Override
    public HTMLElement getElement() {
        return inventoryItem;
    }
}

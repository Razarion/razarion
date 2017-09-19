package com.btxtech.client.editor.inventory;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.I18nStringWidget;
import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import com.btxtech.client.editor.widgets.itemtype.base.BaseItemTypeWidget;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 24.08.2016.
 */
@Templated("InventoryItemPropertyPanel.html#inventory-item-property-panel")
public class InventoryItemPropertyPanel extends AbstractPropertyPanel<InventoryItem> {
    @Inject
    @AutoBound
    private DataBinder<InventoryItem> inventoryItemDataBinder;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private TextBox internalName;
    @Inject
    @DataField
    private I18nStringWidget i18nName;
    @Inject
    @DataField
    private ImageItemWidget thumbnail;
    @Inject
    @DataField
    private BaseItemTypeWidget baseItemType;
    @Inject
    @Bound
    @DataField
    private NumberInput baseItemTypeCount;
    @Inject
    @Bound
    @DataField
    private NumberInput baseItemTypeFreeRange;
    @Inject
    @Bound
    @DataField
    private NumberInput crystalCost;

    @Override
    public void init(InventoryItem inventoryItem) {
        inventoryItemDataBinder.setModel(inventoryItem);
        i18nName.init(inventoryItem.getI18nName(), inventoryItem::setI18nName);
        thumbnail.setImageId(inventoryItem.getImageId(), inventoryItem::setImageId);
        baseItemType.init(inventoryItem.getBaseItemTypeId(), inventoryItem::setBaseItemTypeId);
    }

    @Override
    public InventoryItem getConfigObject() {
        return inventoryItemDataBinder.getModel();
    }
}

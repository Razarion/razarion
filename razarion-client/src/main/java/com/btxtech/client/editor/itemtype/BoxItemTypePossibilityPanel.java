package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.widgets.itemtype.inventoryitem.InventoryItemWidget;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 18.09.2017.
 */
@Templated("BoxItemTypePossibilityPanel.html#boxItemTypePossibilityPanel")
public class BoxItemTypePossibilityPanel extends Composite implements TakesValue<BoxItemTypePossibility> {
    @Inject
    @AutoBound
    private DataBinder<BoxItemTypePossibility> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput possibility;
    @Inject
    @DataField
    private InventoryItemWidget inventoryItem;
    @Inject
    @Bound
    @DataField
    private NumberInput crystals;

    @Override
    public void setValue(BoxItemTypePossibility boxItemTypePossibility) {
        dataBinder.setModel(boxItemTypePossibility);
        inventoryItem.init(boxItemTypePossibility.getInventoryItemId(), boxItemTypePossibility::setInventoryItemId);
    }

    @Override
    public BoxItemTypePossibility getValue() {
        return dataBinder.getModel();
    }
}

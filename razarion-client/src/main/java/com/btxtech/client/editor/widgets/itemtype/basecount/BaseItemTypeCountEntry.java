package com.btxtech.client.editor.widgets.itemtype.basecount;

import com.btxtech.client.editor.widgets.itemtype.base.BaseItemTypeWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 07.08.2017.
 */
@Templated("BaseItemTypeCountWidget.html#baseItemTypeCountRow")
public class BaseItemTypeCountEntry implements TakesValue<ItemTypeCountModel>, IsElement {
    @Inject
    @AutoBound
    private DataBinder<ItemTypeCountModel> dataBinder;
    @Inject
    @DataField
    private TableRow baseItemTypeCountRow;
    @Inject
    @Bound
    @DataField
    private NumberInput count;
    @Inject
    @DataField
    private BaseItemTypeWidget baseItemType;
    @Inject
    @DataField
    private Button baseItemTypeCountDeleteButton;
    private ItemTypeCountModel itemTypeCountModel;

    @Override
    public void setValue(ItemTypeCountModel itemTypeCountModel) {
        this.itemTypeCountModel = itemTypeCountModel;
        baseItemType.init(itemTypeCountModel.getItemType(), itemTypeCountModel::setItemType);
        dataBinder.setModel(itemTypeCountModel);
    }

    @Override
    public ItemTypeCountModel getValue() {
        return itemTypeCountModel;
    }

    @Override
    public HTMLElement getElement() {
        return baseItemTypeCountRow;
    }

    @EventHandler("baseItemTypeCountDeleteButton")
    private void baseItemTypeCountDeleteButtonClick(ClickEvent event) {
        itemTypeCountModel.remove();
    }
}

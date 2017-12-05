package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.widgets.itemtype.baselist.BaseItemTypeListWidget;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemContainerType;
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
 * on 22.08.2017.
 */
@Templated("ItemContainerTypePanel.html#itemContainerTypePanel")
public class ItemContainerTypePanel extends Composite implements TakesValue<ItemContainerType> {
    @Inject
    @AutoBound
    private DataBinder<ItemContainerType> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput range;
    @Inject
    @Bound
    @DataField
    private NumberInput maxCount;
    @Inject
    @DataField
    private BaseItemTypeListWidget ableToContain;

    @Override
    public void setValue(ItemContainerType itemContainerType) {
        dataBinder.setModel(itemContainerType);
        ableToContain.init(itemContainerType.getAbleToContain(), itemContainerType::setAbleToContain);
    }

    @Override
    public ItemContainerType getValue() {
        return dataBinder.getModel();
    }
}

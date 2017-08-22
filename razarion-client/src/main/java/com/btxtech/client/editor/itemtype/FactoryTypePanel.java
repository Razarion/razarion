package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.widgets.itemtype.baselist.BaseItemTypeListWidget;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
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
@Templated("FactoryTypePanel.html#factoryTypePanel")
public class FactoryTypePanel extends Composite implements TakesValue<FactoryType> {
    @Inject
    @AutoBound
    private DataBinder<FactoryType> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput progress;
    @Inject
    @DataField
    private BaseItemTypeListWidget ableToBuildIds;

    @Override
    public void setValue(FactoryType factoryType) {
        dataBinder.setModel(factoryType);
        ableToBuildIds.init(factoryType.getAbleToBuildIds(), factoryType::setAbleToBuildIds);
    }

    @Override
    public FactoryType getValue() {
        return dataBinder.getModel();
    }
}

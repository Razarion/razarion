package com.btxtech.client.editor.itemtype;

import com.btxtech.shared.gameengine.datatypes.itemtype.ConsumerType;
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
@Templated("ConsumerTypePanel.html#consumerTypePanel")
public class ConsumerTypePanel extends Composite implements TakesValue<ConsumerType> {
    @Inject
    @AutoBound
    private DataBinder<ConsumerType> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput wattage;

    @Override
    public void setValue(ConsumerType consumerType) {
        dataBinder.setModel(consumerType);
    }

    @Override
    public ConsumerType getValue() {
        return dataBinder.getModel();
    }
}

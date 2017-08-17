package com.btxtech.client.editor.client.scene;

import com.btxtech.client.editor.widgets.itemtype.base.BaseItemTypeWidget;
import com.btxtech.client.editor.widgets.marker.DecimalPositionWidget;
import com.btxtech.shared.dto.BotMoveCommandConfig;
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
 * on 15.08.2017.
 */
@Templated("BotMoveCommandConfigPropertyPanel.html#botMoveCommandConfigPropertyPanel")
public class BotMoveCommandConfigPropertyPanel extends Composite implements TakesValue<BotMoveCommandConfig> {
    @Inject
    @AutoBound
    private DataBinder<BotMoveCommandConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput botAuxiliaryId;
    @Inject
    @DataField
    private BaseItemTypeWidget baseItemTypeId;
    @Inject
    @Bound
    @DataField
    private DecimalPositionWidget targetPosition;

    @Override
    public void setValue(BotMoveCommandConfig botMoveCommandConfig) {
        dataBinder.setModel(botMoveCommandConfig);
        baseItemTypeId.init(botMoveCommandConfig.getBaseItemTypeId(), botMoveCommandConfig::setBaseItemTypeId);
    }

    @Override
    public BotMoveCommandConfig getValue() {
        return dataBinder.getModel();
    }
}

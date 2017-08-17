package com.btxtech.client.editor.client.scene;

import com.btxtech.client.editor.widgets.itemtype.base.BaseItemTypeWidget;
import com.btxtech.shared.dto.BotRemoveOwnItemCommandConfig;
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
 * on 17.08.2017.
 */
@Templated("BotRemoveOwnItemCommandConfigPropertyPanel.html#botRemoveOwnItemCommandConfigPropertyPanel")
public class BotRemoveOwnItemCommandConfigPropertyPanel extends Composite implements TakesValue<BotRemoveOwnItemCommandConfig> {
    @Inject
    @AutoBound
    private DataBinder<BotRemoveOwnItemCommandConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput botAuxiliaryId;
    @Inject
    @DataField
    private BaseItemTypeWidget baseItemType2RemoveId;

    @Override
    public void setValue(BotRemoveOwnItemCommandConfig botRemoveOwnItemCommandConfig) {
        dataBinder.setModel(botRemoveOwnItemCommandConfig);
        baseItemType2RemoveId.init(botRemoveOwnItemCommandConfig.getBaseItemType2RemoveId(), botRemoveOwnItemCommandConfig::setBaseItemType2RemoveId);
    }

    @Override
    public BotRemoveOwnItemCommandConfig getValue() {
        return dataBinder.getModel();
    }
}

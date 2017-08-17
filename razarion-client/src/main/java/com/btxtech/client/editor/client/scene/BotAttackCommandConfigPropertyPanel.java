package com.btxtech.client.editor.client.scene;

import com.btxtech.client.editor.widgets.itemtype.base.BaseItemTypeWidget;
import com.btxtech.client.editor.widgets.placeconfig.PlaceConfigWidget;
import com.btxtech.shared.dto.BotAttackCommandConfig;
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
@Templated("BotAttackCommandConfigPropertyPanel.html#botAttackCommandConfigPropertyPanel")
public class BotAttackCommandConfigPropertyPanel extends Composite implements TakesValue<BotAttackCommandConfig> {
    @Inject
    @AutoBound
    private DataBinder<BotAttackCommandConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput botAuxiliaryId;
    @Inject
    @DataField
    private BaseItemTypeWidget actorItemTypeId;
    @Inject
    @DataField
    private BaseItemTypeWidget targetItemTypeId;
    @Inject
    @Bound
    @DataField
    private PlaceConfigWidget targetSelection;

    @Override
    public void setValue(BotAttackCommandConfig botAttackCommandConfig) {
        dataBinder.setModel(botAttackCommandConfig);
        actorItemTypeId.init(botAttackCommandConfig.getActorItemTypeId(), botAttackCommandConfig::setActorItemTypeId);
        targetItemTypeId.init(botAttackCommandConfig.getTargetItemTypeId(), botAttackCommandConfig::setTargetItemTypeId);
    }

    @Override
    public BotAttackCommandConfig getValue() {
        return dataBinder.getModel();
    }
}

package com.btxtech.client.editor.client.scene;

import com.btxtech.client.editor.widgets.itemtype.base.BaseItemTypeWidget;
import com.btxtech.client.editor.widgets.placeconfig.PlaceConfigWidget;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;
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
@Templated("BotKillHumanCommandConfigPropertyPanel.html#botKillHumanCommandConfigPropertyPanel")
public class BotKillHumanCommandConfigPropertyPanel extends Composite implements TakesValue<BotKillHumanCommandConfig> {
    @Inject
    @AutoBound
    private DataBinder<BotKillHumanCommandConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput botAuxiliaryId;
    @Inject
    @DataField
    private BaseItemTypeWidget attackerBaseItemTypeId;
    @Inject
    @Bound
    @DataField
    private NumberInput dominanceFactor;
    @Inject
    @Bound
    @DataField
    private PlaceConfigWidget spawnPoint;

    @Override
    public void setValue(BotKillHumanCommandConfig botKillHumanCommandConfig) {
        dataBinder.setModel(botKillHumanCommandConfig);
        attackerBaseItemTypeId.init(botKillHumanCommandConfig.getAttackerBaseItemTypeId(), botKillHumanCommandConfig::setAttackerBaseItemTypeId);
    }

    @Override
    public BotKillHumanCommandConfig getValue() {
        return dataBinder.getModel();
    }
}

package com.btxtech.client.editor.client.scene;

import com.btxtech.client.editor.widgets.itemtype.base.BaseItemTypeWidget;
import com.btxtech.client.editor.widgets.placeconfig.PlaceConfigWidget;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
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
@Templated("BotKillOtherBotCommandConfigPropertyPanel.html#botKillOtherBotCommandConfigPropertyPanel")
public class BotKillOtherBotCommandConfigPropertyPanel extends Composite implements TakesValue<BotKillOtherBotCommandConfig> {
    @Inject
    @AutoBound
    private DataBinder<BotKillOtherBotCommandConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput botAuxiliaryId;
    @Inject
    @Bound
    @DataField
    private NumberInput targetBotAuxiliaryId;
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
    public void setValue(BotKillOtherBotCommandConfig botKillOtherBotCommandConfig) {
        dataBinder.setModel(botKillOtherBotCommandConfig);
        attackerBaseItemTypeId.init(botKillOtherBotCommandConfig.getAttackerBaseItemTypeId(), botKillOtherBotCommandConfig::setAttackerBaseItemTypeId);
    }

    @Override
    public BotKillOtherBotCommandConfig getValue() {
        return dataBinder.getModel();
    }
}

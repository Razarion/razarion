package com.btxtech.client.editor.server.botscene;


import com.btxtech.client.editor.widgets.bot.BotConfigPropertyPanel;
import com.btxtech.client.editor.widgets.bot.BotEnragementStateConfigPropertyPanel;
import com.btxtech.client.editor.widgets.childtable.ChildTable;
import com.btxtech.client.editor.widgets.itemtype.base.BaseItemTypeWidget;
import com.btxtech.client.editor.widgets.placeconfig.PlaceConfigWidget;
import com.btxtech.client.utils.HumanReadableIntegerSizeConverter;
import com.btxtech.client.utils.StringToIntTime;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConflictConfig;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import elemental2.dom.HTMLInputElement;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.common.client.dom.TextInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 16.08.2017.
 */
@Templated("BotSceneConflictConfigPropertyPanel.html#botsceneconfigpanel")
public class BotSceneConflictConfigPropertyPanel extends Composite implements TakesValue<BotSceneConflictConfig> {
    @Inject
    @AutoBound
    private DataBinder<BotSceneConflictConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private HTMLInputElement enterKills;
    @Inject
    @Bound(converter = StringToIntTime.class)
    @DataField
    private HTMLInputElement enterDuration;
    @Inject
    @Bound(converter = StringToIntTime.class)
    @DataField
    private HTMLInputElement leaveNoKillDuration;
    @Inject
    @Bound(converter = StringToIntTime.class)
    @DataField
    private HTMLInputElement rePopMillis;
    @Inject
    @Bound
    @DataField
    private HTMLInputElement minDistance;
    @Inject
    @Bound
    @DataField
    private HTMLInputElement maxDistance;
    @Inject
    @DataField
    private BaseItemTypeWidget targetBaseItemTypeId;
    @Inject
    @Bound
    @DataField
    private HTMLInputElement stopKills;
    @Inject
    @Bound(converter = StringToIntTime.class)
    @DataField
    private HTMLInputElement stopMillis;
    @Inject
    @DataField
    private BotConfigPropertyPanel botConfigPropertyPanel;

    @Override
    public void setValue(BotSceneConflictConfig botSceneConflictConfig) {
        dataBinder.setModel(botSceneConflictConfig);
        targetBaseItemTypeId.init(botSceneConflictConfig.getTargetBaseItemTypeId(), botSceneConflictConfig::setTargetBaseItemTypeId);
        botConfigPropertyPanel.setValue(botSceneConflictConfig.getBotConfig());
    }

    @Override
    public BotSceneConflictConfig getValue() {
        return dataBinder.getModel();
    }
}

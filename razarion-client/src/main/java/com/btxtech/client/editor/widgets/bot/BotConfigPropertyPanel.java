package com.btxtech.client.editor.widgets.bot;


import com.btxtech.client.editor.widgets.childtable.ChildTable;
import com.btxtech.client.editor.widgets.placeconfig.PlaceConfigWidget;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
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
@Templated("BotConfigPropertyPanel.html#botconfigpanel")
public class BotConfigPropertyPanel extends Composite implements TakesValue<BotConfig> {
    @Inject
    @AutoBound
    private DataBinder<BotConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private TextInput internalName;
    @Inject
    @Bound
    @DataField
    private TextInput name;
    @Inject
    @Bound
    @DataField
    private CheckboxInput autoAttack;
    @Inject
    @Bound
    @DataField
    private NumberInput auxiliaryId;
    @Inject
    @Bound
    @DataField
    private CheckboxInput npc;
    @Inject
    @Bound
    @DataField
    private NumberInput actionDelay;
    @Inject
    @Bound
    @DataField
    private PlaceConfigWidget realm;
    @Inject
    @Bound
    @DataField
    private NumberInput minInactiveMs;
    @Inject
    @Bound
    @DataField
    private NumberInput maxInactiveMs;
    @Inject
    @Bound
    @DataField
    private NumberInput minActiveMs;
    @Inject
    @Bound
    @DataField
    private NumberInput maxActiveMs;
    @Inject
    @DataField
    private ChildTable<BotEnragementStateConfig> botEnragementStateConfig;

    @Override
    public void setValue(BotConfig botConfig) {
        dataBinder.setModel(botConfig);
        botEnragementStateConfig.init(botConfig.getBotEnragementStateConfigs(), botConfig::setBotEnragementStateConfigs, BotEnragementStateConfig::new, BotEnragementStateConfigPropertyPanel.class);
    }

    @Override
    public BotConfig getValue() {
        return dataBinder.getModel();
    }
}

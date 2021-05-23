package com.btxtech.client.editor.widgets.bot;


import com.btxtech.client.editor.widgets.childtable.ChildTable;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
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
@Templated("BotEnragementStateConfigPropertyPanel.html#botenragementstateconfigpanel")
public class BotEnragementStateConfigPropertyPanel extends Composite implements TakesValue<BotEnragementStateConfig> {
    @Inject
    @AutoBound
    private DataBinder<BotEnragementStateConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private TextInput name;
    @Inject
    @Bound
    @DataField
    private NumberInput enrageUpKills;
    @Inject
    @DataField
    private ChildTable<BotItemConfig> botItems;

    @Override
    public void setValue(BotEnragementStateConfig botEnragementStateConfig) {
        dataBinder.setModel(botEnragementStateConfig);
        botItems.init(botEnragementStateConfig.getBotItems(), botEnragementStateConfig::botItems, BotItemConfig::new, BotItemConfigPropertyPanel.class);
    }

    @Override
    public BotEnragementStateConfig getValue() {
        return dataBinder.getModel();
    }
}

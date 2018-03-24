package com.btxtech.client.editor.server.botscene;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.bot.selector.BotListWidget;
import com.btxtech.client.editor.widgets.childtable.ChildTable;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConflictConfig;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("BotScenePropertyPanel.html#propertyPanel")
public class BotScenePropertyPanel extends AbstractPropertyPanel<BotSceneConfig> {
    @Inject
    @AutoBound
    private DataBinder<BotSceneConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    @Named("span")
    private HTMLElement id;
    @Inject
    @Bound
    @DataField
    private HTMLInputElement internalName;
    @Inject
    @Bound
    @DataField
    private HTMLInputElement killThreshold;
    @Inject
    @DataField
    private BotListWidget botIdsToWatch;
    @Inject
    @DataField
    private ChildTable<BotSceneConflictConfig> botSceneConflictConfigs;

    @Override
    public void init(BotSceneConfig botSceneConfig) {
        dataBinder.setModel(botSceneConfig);
        botIdsToWatch.init(botSceneConfig.getBotIdsToWatch(), botSceneConfig::setBotIdsToWatch);
        botSceneConflictConfigs.init(botSceneConfig.getBotSceneConflictConfigs(), botSceneConfig::setBotSceneConflictConfigs, () -> new BotSceneConflictConfig().setBotConfig(new BotConfig()), BotSceneConflictConfigPropertyPanel.class);
    }

    @Override
    public BotSceneConfig getConfigObject() {
        return dataBinder.getModel();
    }
}

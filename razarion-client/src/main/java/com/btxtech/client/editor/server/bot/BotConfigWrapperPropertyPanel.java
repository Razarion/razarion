package com.btxtech.client.editor.server.bot;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.bot.BotConfigPropertyPanel;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("BotConfigWrapperPropertyPanel.html#botConfigWrapperPropertyPanel")
public class BotConfigWrapperPropertyPanel extends AbstractPropertyPanel<BotConfig> {
    @Inject
    @DataField
    private BotConfigPropertyPanel configPropertyPanel;

    @Override
    public void init(BotConfig botConfig) {
        configPropertyPanel.setValue(botConfig);
    }

    @Override
    public BotConfig getConfigObject() {
        return configPropertyPanel.getValue();
    }
}

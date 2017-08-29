package com.btxtech.client.editor.server.bot;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.rest.ServerGameEngineControlProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("../../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class BotSidebar extends AbstractCrudeParentSidebar<BotConfig, BotConfigWrapperPropertyPanel> {
    private Logger logger = Logger.getLogger(BotSidebar.class.getName());
    @Inject
    private Instance<BotConfigWrapperPropertyPanel> propertyPanelInstance;
    @Inject
    private BotConfigCrudEditor resourceRegionCrudEditor;
    @Inject
    private Caller<ServerGameEngineControlProvider> provider;

    @Override
    public void onConfigureDialog() {
        super.onConfigureDialog();
        getSideBarPanel().addButton("Restart", () -> provider.call(ignore -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Calling ServerGameEngineControlProvider.restartBots() failed: " + message, throwable);
            return false;
        }).restartBots());
    }

    @Override
    protected CrudEditor<BotConfig> getCrudEditor() {
        return resourceRegionCrudEditor;
    }

    @Override
    protected BotConfigWrapperPropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }

}

package com.btxtech.client.editor.server.botscene;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.client.editor.server.bot.BotConfigWrapperPropertyPanel;
import com.btxtech.client.editor.server.resource.ResourceRegionPropertyPanel;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConfig;
import com.btxtech.shared.rest.ServerGameEngineControlProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("../../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class BotSceneSidebar extends AbstractCrudeParentSidebar<BotSceneConfig, BotScenePropertyPanel> {
    // private Logger logger = Logger.getLogger(BotSceneSidebar.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Instance<BotScenePropertyPanel> propertyPanelInstance;
    @Inject
    private BotSceneConfigCrudEditor botSceneConfigCrudEditor;
    @Inject
    private Caller<ServerGameEngineControlProvider> provider;

    @Override
    public void onConfigureDialog() {
        super.onConfigureDialog();
        getSideBarPanel().addButton("Restart", () -> provider.call(ignore -> {
        }, exceptionHandler.restErrorHandler("Calling ServerGameEngineControlProvider.restartBots() failed: ")).restartBots());
    }

    @Override
    protected CrudEditor<BotSceneConfig> getCrudEditor() {
        return botSceneConfigCrudEditor;
    }

    @Override
    protected BotScenePropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }

}

package com.btxtech.client.editor.level;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
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
@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class LevelConfigSidebar extends AbstractCrudeParentSidebar<LevelConfig, LevelConfigPropertyPanel> {
    private Logger logger = Logger.getLogger(LevelConfigSidebar.class.getName());
    @Inject
    private Instance<LevelConfigPropertyPanel> instance;
    @Inject
    private LevelConfigCrudEditor levelConfigCrudEditor;
    @Inject
    private Caller<ServerGameEngineControlProvider> provider;

    @Override
    public void onConfigureDialog() {
        super.onConfigureDialog();
        getSideBarPanel().addButton("Restart", () -> provider.call(ignore -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Calling ServerGameEngineControlProvider.reloadStatic() failed: " + message, throwable);
            return false;
        }).reloadStatic());
    }

    @Override
    protected CrudEditor<LevelConfig> getCrudEditor() {
        return levelConfigCrudEditor;
    }

    @Override
    protected LevelConfigPropertyPanel createPropertyPanel() {
        return instance.get();
    }
}

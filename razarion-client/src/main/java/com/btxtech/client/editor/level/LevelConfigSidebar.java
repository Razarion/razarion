package com.btxtech.client.editor.level;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.gameengine.datatypes.config.LevelEditConfig;
import com.btxtech.shared.rest.ServerGameEngineControlProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class LevelConfigSidebar extends AbstractCrudeParentSidebar<LevelEditConfig, LevelConfigPropertyPanel> {
    // private Logger logger = Logger.getLogger(LevelConfigSidebar.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Instance<LevelConfigPropertyPanel> instance;
    @Inject
    private LevelConfigCrudEditor levelConfigCrudEditor;
    @Inject
    private Caller<ServerGameEngineControlProvider> provider;

    @Override
    public void onConfigureDialog() {
        super.onConfigureDialog();
        getEditorPanel().addButton("Restart", () -> provider.call(ignore -> {
        }, exceptionHandler.restErrorHandler("Calling ServerGameEngineControlProvider.reloadStatic() failed: ")).reloadStatic());
    }

    @Override
    protected CrudEditor<LevelEditConfig> getCrudEditor() {
        return levelConfigCrudEditor;
    }

    @Override
    protected LevelConfigPropertyPanel createPropertyPanel() {
        return instance.get();
    }
}

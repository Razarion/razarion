package com.btxtech.client.editor.client.scene;

import com.btxtech.client.editor.framework.AbstractObjectNameIdEditor;
import com.btxtech.client.editor.framework.ObjectNameIdTable;
import com.btxtech.client.editor.sidebar.AbstractEditor;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.rest.SceneEditorProvider;
import com.btxtech.uiservice.control.GameUiControl;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("SceneConfigSidebar.html#sceneconfig")
public class SceneConfigSidebar extends AbstractEditor {
    private Logger logger = Logger.getLogger(SceneConfigSidebar.class.getName());
    @Inject
    private Caller<SceneEditorProvider> provider;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    @DataField
    private ObjectNameIdTable sceneConfigTable;

    @PostConstruct
    public void postConstruct() {
        if (gameUiControl.getColdGameUiContext().getWarmGameUiContext().getGameEngineMode() != GameEngineMode.MASTER) {
            throw new IllegalStateException("Only WarmGameUiContext with GameEngineMode.MASTER can have scenes");
        }
        int gameUiControlConfigId = gameUiControl.getColdGameUiContext().getWarmGameUiContext().getGameUiControlConfigId();

        sceneConfigTable.init(new AbstractObjectNameIdEditor() {

            @Override
            protected void read(RemoteCallback<List<ObjectNameId>> callback) {
                provider.call(callback, (message, throwable) -> {
                    logger.log(Level.SEVERE, "SceneEditorProvider.readSceneConfigObjectNameIds failed: " + message, throwable);
                    return false;
                }).readSceneConfigObjectNameIds(gameUiControlConfigId);
            }

            @Override
            protected void create(RemoteCallback<?> callback) {
                provider.call(callback, (message, throwable) -> {
                    logger.log(Level.SEVERE, "SceneEditorProvider.createSceneConfig failed: " + message, throwable);
                    return false;
                }).createSceneConfig(gameUiControlConfigId);
            }

            @Override
            protected void swap(int index1, int index2, RemoteCallback<?> callback) {
                provider.call(callback, (message, throwable) -> {
                    logger.log(Level.SEVERE, "SceneEditorProvider.swapSceneConfig failed: " + message, throwable);
                    return false;
                }).swapSceneConfig(gameUiControlConfigId, index1, index2);
            }

            @Override
            protected void delete(ObjectNameId objectNameId, RemoteCallback<?> callback) {
                provider.call(callback, (message, throwable) -> {
                    logger.log(Level.SEVERE, "SceneEditorProvider.deleteSceneConfig failed: " + message, throwable);
                    return false;
                }).deleteSceneConfig(gameUiControlConfigId, objectNameId.getId());
            }

            @Override
            protected Class<SceneConfigPropertyPanel> getObjectNamePropertyPanelClass() {
                return SceneConfigPropertyPanel.class;
            }
        });
    }
}

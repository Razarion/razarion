package com.btxtech.server.rest;

import com.btxtech.server.persistence.SceneEditorPersistence;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.rest.SceneEditorProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 16.05.2017.
 */
public class SceneEditorProviderImpl implements SceneEditorProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private SceneEditorPersistence sceneEditorPersistence;
    @Inject
    private SessionHolder sessionHolder;

    @Override
    public void saveAllScenes(int gameUiControlConfigId, List<SceneConfig> sceneConfigs) {
        try {
            sceneEditorPersistence.saveAllScenes(gameUiControlConfigId, sceneConfigs, sessionHolder.getPlayerSession().getLocale());
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}

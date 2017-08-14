package com.btxtech.server.rest;

import com.btxtech.server.persistence.SceneEditorPersistence;
import com.btxtech.shared.dto.ObjectNameId;
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

    @Override
    public List<ObjectNameId> readSceneConfigObjectNameIds(int gameUiControlConfigId) {
        try {
            return sceneEditorPersistence.getSceneConfigCrud(gameUiControlConfigId).readObjectNameIds();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void createSceneConfig(int gameUiControlConfigId) {
        try {
            sceneEditorPersistence.getSceneConfigCrud(gameUiControlConfigId).create();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void swapSceneConfig(int gameUiControlConfigId, int index1, int index2) {
        try {
            sceneEditorPersistence.getSceneConfigCrud(gameUiControlConfigId).swap(index1, index2);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteSceneConfig(int gameUiControlConfigId, int sceneConfigId) {
        try {
            sceneEditorPersistence.getSceneConfigCrud(gameUiControlConfigId).delete(sceneConfigId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public SceneConfig readSceneConfig(int gameUiControlConfigId, int sceneConfigId) {
        try {
            return sceneEditorPersistence.getSceneConfigCrud(gameUiControlConfigId).read(sceneConfigId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateSceneConfig(int gameUiControlConfigId, SceneConfig sceneConfig) {
        try {
            sceneEditorPersistence.getSceneConfigCrud(gameUiControlConfigId).update(sceneConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}

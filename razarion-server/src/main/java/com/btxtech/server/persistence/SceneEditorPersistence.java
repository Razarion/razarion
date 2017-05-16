package com.btxtech.server.persistence;

import com.btxtech.server.persistence.scene.SceneEntity;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.SceneConfig;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 16.05.2017.
 */
@Singleton
public class SceneEditorPersistence {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @SecurityCheck
    public void saveAllScenes(int gameUiControlConfigId, List<SceneConfig> sceneConfigs) {
        GameUiControlConfigEntity gameUiControlConfigEntity = entityManager.find(GameUiControlConfigEntity.class, gameUiControlConfigId);
        if (gameUiControlConfigEntity == null) {
            throw new IllegalArgumentException("No GameUiControlConfigEntity for gameUiControlConfigId: " + gameUiControlConfigId);
        }
        List<SceneEntity> sceneEntities = gameUiControlConfigEntity.getScenes();
        if (sceneEntities == null) {
            sceneEntities = new ArrayList<>();
        }
        sceneEntities.clear();
        for (SceneConfig sceneConfig : sceneConfigs) {
            SceneEntity sceneEntity = new SceneEntity();
            sceneEntity.fromSceneConfig(sceneConfig);
            sceneEntities.add(sceneEntity);
        }
    }
}

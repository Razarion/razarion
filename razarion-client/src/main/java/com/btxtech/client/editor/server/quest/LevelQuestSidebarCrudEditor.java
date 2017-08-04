package com.btxtech.client.editor.server.quest;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.rest.ServerGameEngineEditorProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@ApplicationScoped
public class LevelQuestSidebarCrudEditor extends AbstractCrudeEditor<ServerLevelQuestConfig> {
    private Logger logger = Logger.getLogger(LevelQuestSidebarCrudEditor.class.getName());
    @Inject
    private Caller<ServerGameEngineEditorProvider> provider;
    private List<ObjectNameId> objectNameIds = new ArrayList<>();

    @Override
    public void init() {
        provider.call(new RemoteCallback<List<ObjectNameId>>() {
            @Override
            public void callback(List<ObjectNameId> objectNameIds) {
                LevelQuestSidebarCrudEditor.this.objectNameIds = objectNameIds;
                fire();
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ServerGameEngineEditorProvider.readLevelQuestConfigObjectNameIds failed: " + message, throwable);
            return false;
        }).readLevelQuestConfigObjectNameIds();
    }

    @Override
    public void create() {
        provider.call(new RemoteCallback<ServerLevelQuestConfig>() {
            @Override
            public void callback(ServerLevelQuestConfig serverLevelQuestConfig) {
                objectNameIds.add(serverLevelQuestConfig.createObjectNameId());
                fire();
                fireSelection(serverLevelQuestConfig.createObjectNameId());
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ServerGameEngineEditorProvider.createLevelQuestConfig failed: " + message, throwable);
            return false;
        }).createLevelQuestConfig();
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return objectNameIds;
    }

    @Override
    public void delete(ServerLevelQuestConfig levelQuestConfig) {
        provider.call(new RemoteCallback<Void>() {
            @Override
            public void callback(Void aVoid) {
                objectNameIds.removeIf(objectNameId -> objectNameId.getId() == levelQuestConfig.getId());
                fire();
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ServerGameEngineEditorProvider.deleteLevelQuestConfig failed: " + message, throwable);
            return false;
        }).deleteLevelQuestConfig(levelQuestConfig.getId());
    }

    @Override
    public void save(ServerLevelQuestConfig serverLevelQuestConfig) {
        provider.call(ignore -> fire(), (message, throwable) -> {
            logger.log(Level.SEVERE, "ServerGameEngineEditorProvider.updateLevelQuestConfig failed: " + message, throwable);
            return false;
        }).updateLevelQuestConfig(serverLevelQuestConfig);
    }

    @Override
    public void reload() {
        init();
    }

    @Override
    public void getInstance(ObjectNameId id, Consumer<ServerLevelQuestConfig> callback) {
        provider.call(new RemoteCallback<ServerLevelQuestConfig>() {
            @Override
            public void callback(ServerLevelQuestConfig levelQuestConfig) {
                callback.accept(levelQuestConfig);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ServerGameEngineEditorProvider.readLevelQuestConfig failed: " + message, throwable);
            return false;
        }).readLevelQuestConfig(id.getId());
    }
}

package com.btxtech.client.editor.server.quest;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
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

/**
 * Created by Beat
 * on 28.07.2017.
 */
@ApplicationScoped
public class LevelQuestSidebarCrudEditor extends AbstractCrudeEditor<ServerLevelQuestConfig> {
    // private Logger logger = Logger.getLogger(LevelQuestSidebarCrudEditor.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
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
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.readLevelQuestConfigObjectNameIds failed: ")).readLevelQuestConfigObjectNameIds();
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
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.createLevelQuestConfig failed: ")).createLevelQuestConfig();
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
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.deleteLevelQuestConfig failed: ")).deleteLevelQuestConfig(levelQuestConfig.getId());
    }

    @Override
    public void save(ServerLevelQuestConfig serverLevelQuestConfig) {
        provider.call(ignore -> fire(), exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.updateLevelQuestConfig failed: ")).updateLevelQuestConfig(serverLevelQuestConfig);
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
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.readLevelQuestConfig failed: ")).readLevelQuestConfig(id.getId());
    }
}

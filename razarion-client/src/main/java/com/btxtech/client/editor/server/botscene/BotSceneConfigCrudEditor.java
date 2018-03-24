package com.btxtech.client.editor.server.botscene;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConfig;
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
public class BotSceneConfigCrudEditor extends AbstractCrudeEditor<BotSceneConfig> {
    // private Logger logger = Logger.getLogger(BotSceneConfigCrudEditor.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Caller<ServerGameEngineEditorProvider> provider;
    private List<ObjectNameId> objectNameIds = new ArrayList<>();

    @Override
    public void init() {
        provider.call((RemoteCallback<List<ObjectNameId>>) objectNameIds -> {
            BotSceneConfigCrudEditor.this.objectNameIds = objectNameIds;
            fire();
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.readBotSceneConfigObjectNameIds failed: ")).readBotSceneConfigObjectNameIds();
    }

    @Override
    public void create() {
        provider.call((RemoteCallback<BotSceneConfig>) botSceneConfig -> {
            objectNameIds.add(botSceneConfig.createObjectNameId());
            fire();
            fireSelection(botSceneConfig.createObjectNameId());
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.createBotSceneConfig failed: ")).createBotSceneConfig();
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return objectNameIds;
    }

    @Override
    public void delete(BotSceneConfig botSceneConfig) {
        provider.call((RemoteCallback<Void>) aVoid -> {
            objectNameIds.removeIf(objectNameId -> objectNameId.getId() == botSceneConfig.getId());
            fire();
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.deleteBotSceneConfigConfig failed: ")).deleteBotSceneConfigConfig(botSceneConfig.getId());
    }

    @Override
    public void save(BotSceneConfig botSceneConfig) {
        provider.call(ignore -> fire(), exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.updateBotSceneConfig failed: ")).updateBotSceneConfig(botSceneConfig);
    }

    @Override
    public void reload() {
        init();
    }

    @Override
    public void getInstance(ObjectNameId id, Consumer<BotSceneConfig> callback) {
        provider.call((RemoteCallback<BotSceneConfig>) callback::accept, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.readBotSceneConfig failed: ")).readBotSceneConfig(id.getId());
    }
}

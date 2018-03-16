package com.btxtech.client.editor.server.bot;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
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
public class BotConfigCrudEditor extends AbstractCrudeEditor<BotConfig> {
    // private Logger logger = Logger.getLogger(BotConfigCrudEditor.class.getName());
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
                BotConfigCrudEditor.this.objectNameIds = objectNameIds;
                fire();
            }
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.readBotConfigObjectNameIds failed: ")).readBotConfigObjectNameIds();
    }

    @Override
    public void create() {
        provider.call(new RemoteCallback<BotConfig>() {
            @Override
            public void callback(BotConfig botConfig) {
                objectNameIds.add(botConfig.createObjectNameId());
                fire();
                fireSelection(botConfig.createObjectNameId());
            }
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.createBotConfig failed: ")).createBotConfig();
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return objectNameIds;
    }

    @Override
    public void delete(BotConfig botConfig) {
        provider.call(new RemoteCallback<Void>() {
            @Override
            public void callback(Void aVoid) {
                objectNameIds.removeIf(objectNameId -> objectNameId.getId() == botConfig.getId());
                fire();
            }
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.deleteBotConfigConfig failed: ")).deleteBotConfigConfig(botConfig.getId());
    }

    @Override
    public void save(BotConfig botConfig) {
        provider.call(ignore -> fire(), exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.updateBotConfig failed: ")).updateBotConfig(botConfig);
    }

    @Override
    public void reload() {
        init();
    }

    @Override
    public void getInstance(ObjectNameId id, Consumer<BotConfig> callback) {
        provider.call(new RemoteCallback<BotConfig>() {
            @Override
            public void callback(BotConfig botConfig) {
                callback.accept(botConfig);
            }
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.readBotConfig failed: ")).readBotConfig(id.getId());
    }
}

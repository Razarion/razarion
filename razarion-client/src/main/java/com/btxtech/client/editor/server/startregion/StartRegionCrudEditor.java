package com.btxtech.client.editor.server.startregion;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.StartRegionConfig;
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
public class StartRegionCrudEditor extends AbstractCrudeEditor<StartRegionConfig> {
    // private Logger logger = Logger.getLogger(StartRegionCrudEditor.class.getName());
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
                StartRegionCrudEditor.this.objectNameIds = objectNameIds;
                fire();
            }
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.readStartRegionObjectNameIds failed: ")).readStartRegionObjectNameIds();
    }

    @Override
    public void create() {
        provider.call(new RemoteCallback<StartRegionConfig>() {
            @Override
            public void callback(StartRegionConfig startRegionConfig) {
                objectNameIds.add(startRegionConfig.createObjectNameId());
                fire();
                fireSelection(startRegionConfig.createObjectNameId());
            }
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.createStartRegionConfig failed: ")).createStartRegionConfig();
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return objectNameIds;
    }

    @Override
    public void delete(StartRegionConfig startRegionConfig) {
        provider.call(new RemoteCallback<Void>() {
            @Override
            public void callback(Void aVoid) {
                objectNameIds.removeIf(objectNameId -> objectNameId.getId() == startRegionConfig.getId());
                fire();
            }
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.saveStartRegionConfig failed: ")).deleteStartRegionConfig(startRegionConfig.getId());
    }

    @Override
    public void save(StartRegionConfig startRegionConfig) {
        provider.call(ignore -> fire(), exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.updateStartRegionConfig failed: ")).updateStartRegionConfig(startRegionConfig);
    }

    @Override
    public void reload() {
        init();
    }

    @Override
    public void getInstance(ObjectNameId id, Consumer<StartRegionConfig> callback) {
        provider.call(new RemoteCallback<StartRegionConfig>() {
            @Override
            public void callback(StartRegionConfig startRegionConfig) {
                callback.accept(startRegionConfig);
            }
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.readStartRegionConfig failed: ")).readStartRegionConfig(id.getId());
    }
}

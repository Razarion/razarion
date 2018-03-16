package com.btxtech.client.editor.server.resource;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ResourceRegionConfig;
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
public class ResourceRegionCrudEditor extends AbstractCrudeEditor<ResourceRegionConfig> {
    // private Logger logger = Logger.getLogger(ResourceRegionCrudEditor.class.getName());
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
                ResourceRegionCrudEditor.this.objectNameIds = objectNameIds;
                fire();
            }
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.readResourceRegionObjectNameIds failed: ")).readResourceRegionObjectNameIds();
    }

    @Override
    public void create() {
        provider.call(new RemoteCallback<ResourceRegionConfig>() {
            @Override
            public void callback(ResourceRegionConfig resourceRegionConfig) {
                objectNameIds.add(resourceRegionConfig.createObjectNameId());
                fire();
                fireSelection(resourceRegionConfig.createObjectNameId());
            }
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.createResourceRegionConfig failed: ")).createResourceRegionConfig();
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return objectNameIds;
    }

    @Override
    public void delete(ResourceRegionConfig resourceRegionConfig) {
        provider.call(new RemoteCallback<Void>() {
            @Override
            public void callback(Void aVoid) {
                objectNameIds.removeIf(objectNameId -> objectNameId.getId() == resourceRegionConfig.getId());
                fire();
            }
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.deleteResourceRegionConfig failed: ")).deleteResourceRegionConfig(resourceRegionConfig.getId());
    }

    @Override
    public void save(ResourceRegionConfig resourceRegionConfig) {
        provider.call(ignore -> fire(), exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.updateResourceRegionConfig failed: ")).updateResourceRegionConfig(resourceRegionConfig);
    }

    @Override
    public void reload() {
        init();
    }

    @Override
    public void getInstance(ObjectNameId id, Consumer<ResourceRegionConfig> callback) {
        provider.call(new RemoteCallback<ResourceRegionConfig>() {
            @Override
            public void callback(ResourceRegionConfig resourceRegionConfig) {
                callback.accept(resourceRegionConfig);
            }
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.readResourceRegionConfig failed: ")).readResourceRegionConfig(id.getId());
    }
}

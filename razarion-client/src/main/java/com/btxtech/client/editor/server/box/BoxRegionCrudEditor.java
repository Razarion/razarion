package com.btxtech.client.editor.server.box;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.ObjectNameId;
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
public class BoxRegionCrudEditor extends AbstractCrudeEditor<BoxRegionConfig> {
    // private Logger logger = Logger.getLogger(BoxRegionCrudEditor.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Caller<ServerGameEngineEditorProvider> provider;
    private List<ObjectNameId> objectNameIds = new ArrayList<>();

    @Override
    public void init() {
        provider.call((RemoteCallback<List<ObjectNameId>>) objectNameIds -> {
            BoxRegionCrudEditor.this.objectNameIds = objectNameIds;
            fire();
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.readBoxRegionObjectNameIds failed: ")).readBoxRegionObjectNameIds();
    }

    @Override
    public void create() {
        provider.call((RemoteCallback<BoxRegionConfig>) boxRegionConfig -> {
            objectNameIds.add(boxRegionConfig.createObjectNameId());
            fire();
            fireSelection(boxRegionConfig.createObjectNameId());
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.createBoxRegionConfig failed: ")).createBoxRegionConfig();
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return objectNameIds;
    }

    @Override
    public void delete(BoxRegionConfig boxRegionConfig) {
        provider.call((RemoteCallback<Void>) aVoid -> {
            objectNameIds.removeIf(objectNameId -> objectNameId.getId() == boxRegionConfig.getId());
            fire();
        }, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.deleteBoxRegionConfig failed: ")).deleteBoxRegionConfig(boxRegionConfig.getId());
    }

    @Override
    public void save(BoxRegionConfig boxRegionConfig) {
        provider.call(ignore -> fire(), exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.updateBoxRegionConfig failed: ")).updateBoxRegionConfig(boxRegionConfig);
    }

    @Override
    public void reload() {
        init();
    }

    @Override
    public void getInstance(ObjectNameId id, Consumer<BoxRegionConfig> callback) {
        provider.call((RemoteCallback<BoxRegionConfig>) callback::accept, exceptionHandler.restErrorHandler("ServerGameEngineEditorProvider.readBoxRegionConfig failed: ")).readBoxRegionConfig(id.getId());
    }
}

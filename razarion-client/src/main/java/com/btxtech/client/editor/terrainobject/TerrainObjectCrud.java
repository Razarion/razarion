package com.btxtech.client.editor.terrainobject;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.rest.TerrainElementEditorProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 22.08.2016.
 */
@ApplicationScoped
public class TerrainObjectCrud extends AbstractCrudeEditor<TerrainObjectConfig> {
    // private Logger logger = Logger.getLogger(TerrainObjectCrud.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private Caller<TerrainElementEditorProvider> provider;

    @Override
    public void create() {
        provider.call(new RemoteCallback<TerrainObjectConfig>() {
            @Override
            public void callback(TerrainObjectConfig terrainObjectConfig) {
                terrainTypeService.overrideTerrainObjectConfig(terrainObjectConfig);
                fire();
                fireSelection(terrainObjectConfig.createObjectNameId());
            }
        }, exceptionHandler.restErrorHandler("TerrainElementEditorProvider.createTerrainObjectConfig failed: ")).createTerrainObjectConfig();
    }

    @Override
    public void reload() {
        provider.call(new RemoteCallback<List<TerrainObjectConfig>>() {
            @Override
            public void callback(List<TerrainObjectConfig> terrainObjectConfig) {
                terrainTypeService.setTerrainObjectConfigs(terrainObjectConfig);
                fire();
                fireChange(terrainObjectConfig);
            }
        }, exceptionHandler.restErrorHandler("TerrainElementEditorProvider.createTerrainObjectConfig failed: ")).readTerrainObjectConfigs();
    }

    @Override
    public void getInstance(ObjectNameId objectNameId, Consumer<TerrainObjectConfig> callback) {
        callback.accept(terrainTypeService.getTerrainObjectConfig(objectNameId.getId()));
    }

    @Override
    public void save(TerrainObjectConfig terrainObjectConfig) {
        provider.call(ignore -> fire(), exceptionHandler.restErrorHandler("TerrainElementEditorProvider.saveTerrainObjectConfig failed: ")).saveTerrainObjectConfig(terrainObjectConfig);
    }


    @Override
    public void delete(TerrainObjectConfig terrainObjectConfig) {
        provider.call(ignore -> {
            terrainTypeService.deleteTerrainObjectConfig(terrainObjectConfig);
            fire();
        }, exceptionHandler.restErrorHandler("TerrainElementEditorProvider.deleteTerrainObjectConfig failed: ")).deleteTerrainObjectConfig(terrainObjectConfig);
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return terrainTypeService.getTerrainObjectConfigs().stream().map(TerrainObjectConfig::createObjectNameId).collect(Collectors.toList());
    }

}

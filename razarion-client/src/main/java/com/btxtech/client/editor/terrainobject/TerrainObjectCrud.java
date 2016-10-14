package com.btxtech.client.editor.terrainobject;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.shared.rest.TerrainElementEditorProvider;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 22.08.2016.
 */
@ApplicationScoped
public class TerrainObjectCrud extends AbstractCrudeEditor<TerrainObjectConfig> {
    private Logger logger = Logger.getLogger(TerrainObjectCrud.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    @SuppressWarnings("CdiInjectionPointsInspection")
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
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "TerrainElementEditorProvider.createTerrainObjectConfig failed: " + message, throwable);
            return false;
        }).createTerrainObjectConfig();
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
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "TerrainElementEditorProvider.createTerrainObjectConfig failed: " + message, throwable);
            return false;
        }).readTerrainObjectConfigs();
    }

    @Override
    public TerrainObjectConfig getInstance(ObjectNameId objectNameId) {
        return terrainTypeService.getTerrainObjectConfig(objectNameId.getId());
    }

    @Override
    public void save(TerrainObjectConfig terrainObjectConfig) {
        provider.call(ignore -> fire(), (message, throwable) -> {
            logger.log(Level.SEVERE, "TerrainElementEditorProvider.saveTerrainObjectConfig failed: " + message, throwable);
            return false;
        }).saveTerrainObjectConfig(terrainObjectConfig);
    }


    @Override
    public void delete(TerrainObjectConfig terrainObjectConfig) {
        provider.call(ignore -> {
            terrainTypeService.deleteTerrainObjectConfig(terrainObjectConfig);
            fire();
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "TerrainElementEditorProvider.deleteTerrainObjectConfig failed: " + message, throwable);
            return false;
        }).deleteTerrainObjectConfig(terrainObjectConfig);
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return terrainTypeService.getTerrainObjectConfigs().stream().map(TerrainObjectConfig::createObjectNameId).collect(Collectors.toList());
    }

}

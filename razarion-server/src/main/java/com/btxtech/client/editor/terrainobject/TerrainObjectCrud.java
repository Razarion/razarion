package com.btxtech.client.editor.terrainobject;

import com.btxtech.shared.TerrainElementEditorProvider;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 22.08.2016.
 */
@ApplicationScoped
public class TerrainObjectCrud {
    private Logger logger = Logger.getLogger(TerrainObjectCrud.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<TerrainElementEditorProvider> provider;
    private Collection<Consumer<List<ObjectNameId>>> observers = new ArrayList<>();

    public void monitor(Consumer<List<ObjectNameId>> observer) {
        observers.add(observer);
        observer.accept(setupObjectNameIds());
    }

    public void removeMonitor(Consumer<List<ObjectNameId>> observer) {
        observers.remove(observer);
    }

    public void create() {
        provider.call(new RemoteCallback<TerrainObjectConfig>() {
            @Override
            public void callback(TerrainObjectConfig terrainObjectConfig) {
                terrainTypeService.overrideTerrainObjectConfig(terrainObjectConfig);
                fire();
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "TerrainElementEditorProvider.createTerrainObjectConfig failed: " + message, throwable);
            return false;
        }).createTerrainObjectConfig();
    }

    public void reload() {
        provider.call(new RemoteCallback<List<TerrainObjectConfig>>() {
            @Override
            public void callback(List<TerrainObjectConfig> terrainObjectConfig) {
                terrainTypeService.setTerrainObjectConfigs(terrainObjectConfig);
                fire();
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "TerrainElementEditorProvider.createTerrainObjectConfig failed: " + message, throwable);
            return false;
        }).readTerrainObjectConfigs();
    }

    public void save(TerrainObjectConfig terrainObjectConfig) {
        provider.call(ignore -> fire(), (message, throwable) -> {
            logger.log(Level.SEVERE, "TerrainElementEditorProvider.saveTerrainObjectConfig failed: " + message, throwable);
            return false;
        }).saveTerrainObjectConfig(terrainObjectConfig);
    }


    public void delete(TerrainObjectConfig terrainObjectConfig) {
        provider.call(ignore -> {
            terrainTypeService.deleteTerrainObjectConfig(terrainObjectConfig);
            fire();
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "TerrainElementEditorProvider.deleteTerrainObjectConfig failed: " + message, throwable);
            return false;
        }).deleteTerrainObjectConfig(terrainObjectConfig);
    }

    private List<ObjectNameId> setupObjectNameIds() {
        return terrainTypeService.getTerrainObjectConfigs().stream().map(TerrainObjectConfig::createSlopeNameId).collect(Collectors.toList());
    }

    private void fire() {
        List<ObjectNameId> objectNameIds = setupObjectNameIds();
        for (Consumer<List<ObjectNameId>> observer : observers) {
            observer.accept(objectNameIds);
        }
    }

}

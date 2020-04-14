package com.btxtech.server.rest;

import com.btxtech.server.persistence.TerrainElementPersistence;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.rest.TerrainElementEditorProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 20.11.2015.
 */
public class TerrainElementEditorProviderImpl implements TerrainElementEditorProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TerrainElementPersistence persistenceService;

    @Override
    public WaterConfig saveWaterConfig(WaterConfig waterConfig) {
        try {
            return persistenceService.saveWaterConfig(waterConfig);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public List<ObjectNameId> getTerrainObjectNameIds() {
        try {
            return persistenceService.getTerrainObjectNameIds();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public TerrainObjectConfig createTerrainObjectConfig() {
        try {
            return persistenceService.createTerrainObjectConfig();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public TerrainObjectConfig readTerrainObjectConfig(int id) {
        try {
            return persistenceService.readTerrainObjectConfig(id);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public List<TerrainObjectConfig> readTerrainObjectConfigs() {
        try {
            return persistenceService.readTerrainObjects();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void saveTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig) {
        try {
            persistenceService.saveTerrainObject(terrainObjectConfig);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void deleteTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig) {
        try {
            persistenceService.deleteTerrainObjectConfig(terrainObjectConfig);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public List<ObjectNameId> readDrivewayObjectNameIds() {
        try {
            return persistenceService.readDrivewayObjectNameIds();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}

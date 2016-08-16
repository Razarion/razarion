package com.btxtech.server.rest;

import com.btxtech.server.persistence.TerrainElementPersistence;
import com.btxtech.shared.TerrainElementEditorProvider;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 20.11.2015.
 */
public class TerrainElementEditorProviderImpl implements TerrainElementEditorProvider {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TerrainElementPersistence persistenceService;

    @Override
    public List<ObjectNameId> getSlopeNameIds() {
        try {
            return persistenceService.getSlopeNameIds();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public SlopeConfig loadSlopeConfig(int id) {
        try {
            return persistenceService.loadSlopeConfig(id);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public SlopeConfig saveSlopeConfig(SlopeConfig slopeConfig) {
        try {
            return persistenceService.saveSlopeConfig(slopeConfig);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void deleteSlopeConfig(SlopeConfig slopeConfig) {
        try {
            persistenceService.deleteSlopeConfig(slopeConfig);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public GroundConfig loadGroundConfig() {
        try {
            return persistenceService.loadGroundConfig();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public GroundConfig saveGroundConfig(GroundConfig slopeConfig) {
        try {
            return persistenceService.saveGroundConfig(slopeConfig);
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
    public TerrainObjectConfig loadTerrainObjectConfig(int id) {
        try {
            return persistenceService.loadTerrainObjectConfig(id);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public TerrainObjectConfig saveTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig) {
        try {
            return persistenceService.saveTerrainObject(terrainObjectConfig);
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
}

package com.btxtech.server.rest;

import com.btxtech.server.DataUrlDecoder;
import com.btxtech.server.gameengine.TerrainShapeService;
import com.btxtech.server.persistence.PlanetPersistence;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.dto.TerrainEditorUpdate;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.rest.PlanetEditorProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by Beat
 * 08.07.2016.
 */
public class PlanetEditorProviderImpl implements PlanetEditorProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private PlanetPersistence persistenceService;
    @Inject
    private TerrainShapeService terrainShapeService;

    @Override
    public void createTerrainObjectPositions(int planetId, List<TerrainObjectPosition> createdTerrainObjects) {
        try {
            persistenceService.createTerrainObjectPositions(planetId, createdTerrainObjects);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateTerrainObjectPositions(int planetId, List<TerrainObjectPosition> updatedTerrainObjects) {
        try {
            persistenceService.updateTerrainObjectPositions(planetId, updatedTerrainObjects);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteTerrainObjectPositionIds(int planetId, List<Integer> deletedTerrainIds) {
        try {
            persistenceService.deleteTerrainObjectPositionIds(planetId, deletedTerrainIds);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public List<TerrainSlopePosition> readTerrainSlopePositions(int planetId) {
        try {
            return persistenceService.getTerrainSlopePositions(planetId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateTerrain(int planetId, TerrainEditorUpdate terrainEditorUpdate) {
        try {
            if (terrainEditorUpdate.getCreatedSlopes() != null) {
                persistenceService.createTerrainSlopePositions(planetId, terrainEditorUpdate.getCreatedSlopes());
            }
            if (terrainEditorUpdate.getUpdatedSlopes() != null) {
                persistenceService.updateTerrainSlopePositions(planetId, terrainEditorUpdate.getUpdatedSlopes());
            }
            if (terrainEditorUpdate.getDeletedSlopeIds() != null) {
                persistenceService.deleteTerrainSlopePositions(planetId, terrainEditorUpdate.getDeletedSlopeIds());
            }
            terrainShapeService.setupTerrainShape(persistenceService.loadPlanetConfig(planetId));
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updatePlanetVisualConfig(int planetId, PlanetVisualConfig planetVisualConfig) {
        try {
            persistenceService.updatePlanetVisualConfig(planetId, planetVisualConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updatePlanetConfig(PlanetConfig planetConfig) {
        try {
            persistenceService.updatePlanetConfig(planetConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateMiniMapImage(int planetId, String dataUrl) {
        try {
            DataUrlDecoder dataUrlDecoder = new DataUrlDecoder(dataUrl);
            persistenceService.updateMiniMapImage(planetId, dataUrlDecoder.getData());
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}

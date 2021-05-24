package com.btxtech.server.rest;

import com.btxtech.server.DataUrlDecoder;
import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.dto.TerrainEditorLoad;
import com.btxtech.shared.dto.TerrainEditorUpdate;
import com.btxtech.shared.rest.TerrainEditorController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;

/**
 * Created by Beat
 * 08.07.2016.
 */
public class TerrainEditorControllerImpl implements TerrainEditorController {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;

    @Override
    public TerrainEditorLoad readTerrainEditorLoad(int planetId) {
        try {
            TerrainEditorLoad terrainEditorLoad = new TerrainEditorLoad();
            terrainEditorLoad.setSlopes(planetCrudPersistence.getTerrainSlopePositions(planetId));
            terrainEditorLoad.setTerrainObjects(planetCrudPersistence.getTerrainObjectPositions(planetId));
            return terrainEditorLoad;
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateTerrain(int planetId, TerrainEditorUpdate terrainEditorUpdate) {
        try {
            // Check if terrain is valid
            // this does not make any sense terrainShapeService.setupTerrainShapeDryRun(planetId, terrainEditorUpdate);

            if (terrainEditorUpdate.getCreatedSlopes() != null && !terrainEditorUpdate.getCreatedSlopes().isEmpty()) {
                planetCrudPersistence.createTerrainSlopePositions(planetId, terrainEditorUpdate.getCreatedSlopes());
            }
            if (terrainEditorUpdate.getUpdatedSlopes() != null && !terrainEditorUpdate.getUpdatedSlopes().isEmpty()) {
                planetCrudPersistence.updateTerrainSlopePositions(planetId, terrainEditorUpdate.getUpdatedSlopes());
            }
            if (terrainEditorUpdate.getDeletedSlopeIds() != null && !terrainEditorUpdate.getDeletedSlopeIds().isEmpty()) {
                planetCrudPersistence.deleteTerrainSlopePositions(planetId, terrainEditorUpdate.getDeletedSlopeIds());
            }

            if (terrainEditorUpdate.getCreatedTerrainObjects() != null && !terrainEditorUpdate.getCreatedTerrainObjects().isEmpty()) {
                planetCrudPersistence.createTerrainObjectPositions(planetId, terrainEditorUpdate.getCreatedTerrainObjects());
            }
            if (terrainEditorUpdate.getUpdatedTerrainObjects() != null && !terrainEditorUpdate.getUpdatedTerrainObjects().isEmpty()) {
                planetCrudPersistence.updateTerrainObjectPositions(planetId, terrainEditorUpdate.getUpdatedTerrainObjects());
            }
            if (terrainEditorUpdate.getDeletedTerrainObjectsIds() != null && !terrainEditorUpdate.getDeletedTerrainObjectsIds().isEmpty()) {
                planetCrudPersistence.deleteTerrainObjectPositionIds(planetId, terrainEditorUpdate.getDeletedTerrainObjectsIds());
            }
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updatePlanetVisualConfig(int planetId, PlanetVisualConfig planetVisualConfig) {
        try {
            planetCrudPersistence.updatePlanetVisualConfig(planetId, planetVisualConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateMiniMapImage(int planetId, String dataUrl) {
        try {
            DataUrlDecoder dataUrlDecoder = new DataUrlDecoder(dataUrl);
            planetCrudPersistence.updateMiniMapImage(planetId, dataUrlDecoder.getData());
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}

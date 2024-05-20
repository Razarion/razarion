package com.btxtech.server.rest;

import com.btxtech.server.DataUrlDecoder;
import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.dto.SlopeTerrainEditorUpdate;
import com.btxtech.shared.dto.TerrainEditorUpdate;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.rest.TerrainEditorController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 08.07.2016.
 */
public class TerrainEditorControllerImpl implements TerrainEditorController {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;
    public static byte[] zippedHeightMap;

    @Override
    public List<TerrainSlopePosition> readTerrainSlopePositions(int planetId) {
        try {
            return planetCrudPersistence.getTerrainSlopePositions(planetId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @SecurityCheck
    public void updateSlopes(int planetId, SlopeTerrainEditorUpdate slopeTerrainEditorUpdate) {
        try {
            // Check if terrain is valid
            // this does not make any sense terrainShapeService.setupTerrainShapeDryRun(planetId, slopeTerrainEditorUpdate);

            if (slopeTerrainEditorUpdate.getCreatedSlopes() != null && !slopeTerrainEditorUpdate.getCreatedSlopes().isEmpty()) {
                planetCrudPersistence.createTerrainSlopePositions(planetId, slopeTerrainEditorUpdate.getCreatedSlopes());
            }
            if (slopeTerrainEditorUpdate.getUpdatedSlopes() != null && !slopeTerrainEditorUpdate.getUpdatedSlopes().isEmpty()) {
                planetCrudPersistence.updateTerrainSlopePositions(planetId, slopeTerrainEditorUpdate.getUpdatedSlopes());
            }
            if (slopeTerrainEditorUpdate.getDeletedSlopeIds() != null && !slopeTerrainEditorUpdate.getDeletedSlopeIds().isEmpty()) {
                planetCrudPersistence.deleteTerrainSlopePositions(planetId, slopeTerrainEditorUpdate.getDeletedSlopeIds());
            }
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @SecurityCheck
    public void updateTerrain(int planetId, TerrainEditorUpdate terrainEditorUpdate) {
        try {
            // Check if terrain is valid
            // this does not make any sense terrainShapeService.setupTerrainShapeDryRun(planetId, terrainEditorUpdate);

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
    @SecurityCheck
    public void updatePlanetVisualConfig(int planetId, PlanetVisualConfig planetVisualConfig) {
        try {
            planetCrudPersistence.updatePlanetVisualConfig(planetId, planetVisualConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @SecurityCheck
    public void updateMiniMapImage(int planetId, String dataUrl) {
        try {
            DataUrlDecoder dataUrlDecoder = new DataUrlDecoder(dataUrl);
            planetCrudPersistence.updateMiniMapImage(planetId, dataUrlDecoder.getData());
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @SecurityCheck
    public void saveTerrainShape(int planetId, byte[] zippedHeightMap) {
        System.out.println(planetId);
        System.out.println(zippedHeightMap.length);
        TerrainEditorControllerImpl.zippedHeightMap = zippedHeightMap;
    }
}

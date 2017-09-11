package com.btxtech.server.rest;

import com.btxtech.server.DataUrlDecoder;
import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.gameengine.TerrainShapeService;
import com.btxtech.server.persistence.PlanetPersistence;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.dto.TerrainEditorUpdate;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.rest.PlanetEditorProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 08.07.2016.
 */
public class PlanetEditorProviderImpl implements PlanetEditorProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private PlanetPersistence planetPersistence;
    @Inject
    private TerrainShapeService terrainShapeService;
    @Inject
    private ServerGameEngineControl serverGameEngineControl;
    @Inject
    private ClientSystemConnectionService systemConnectionService;

    @Override
    public void createTerrainObjectPositions(int planetId, List<TerrainObjectPosition> createdTerrainObjects) {
        try {
            planetPersistence.createTerrainObjectPositions(planetId, createdTerrainObjects);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateTerrainObjectPositions(int planetId, List<TerrainObjectPosition> updatedTerrainObjects) {
        try {
            planetPersistence.updateTerrainObjectPositions(planetId, updatedTerrainObjects);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteTerrainObjectPositionIds(int planetId, List<Integer> deletedTerrainIds) {
        try {
            planetPersistence.deleteTerrainObjectPositionIds(planetId, deletedTerrainIds);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public List<TerrainSlopePosition> readTerrainSlopePositions(int planetId) {
        try {
            return planetPersistence.getTerrainSlopePositions(planetId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateTerrain(int planetId, TerrainEditorUpdate terrainEditorUpdate) {
        try {
            // Check if terrain is valid
            terrainShapeService.setupTerrainShapeDryRun(planetId, terrainEditorUpdate);

            if (terrainEditorUpdate.getCreatedSlopes() != null) {
                planetPersistence.createTerrainSlopePositions(planetId, terrainEditorUpdate.getCreatedSlopes());
            }
            if (terrainEditorUpdate.getUpdatedSlopes() != null) {
                planetPersistence.updateTerrainSlopePositions(planetId, terrainEditorUpdate.getUpdatedSlopes());
            }
            if (terrainEditorUpdate.getDeletedSlopeIds() != null) {
                planetPersistence.deleteTerrainSlopePositions(planetId, terrainEditorUpdate.getDeletedSlopeIds());
            }
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void restartPlanet(int planetId) {
        try {
            systemConnectionService.sendLifecyclePacket(new LifecyclePacket().setType(LifecyclePacket.Type.HOLD).setDialog(LifecyclePacket.Dialog.PLANET_RESTART));
            terrainShapeService.setupTerrainShape(planetPersistence.loadPlanetConfig(planetId));
            serverGameEngineControl.restartPlanet();
            systemConnectionService.sendLifecyclePacket(new LifecyclePacket().setType(LifecyclePacket.Type.RESTART_WARM));
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updatePlanetVisualConfig(int planetId, PlanetVisualConfig planetVisualConfig) {
        try {
            planetPersistence.updatePlanetVisualConfig(planetId, planetVisualConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updatePlanetConfig(PlanetConfig planetConfig) {
        try {
            planetPersistence.updatePlanetConfig(planetConfig);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateMiniMapImage(int planetId, String dataUrl) {
        try {
            DataUrlDecoder dataUrlDecoder = new DataUrlDecoder(dataUrl);
            planetPersistence.updateMiniMapImage(planetId, dataUrlDecoder.getData());
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}

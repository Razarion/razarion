package com.btxtech.server.rest;

import com.btxtech.server.persistence.PlanetPersistenceService;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.rest.PlanetEditorProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Created by Beat
 * 08.07.2016.
 */
public class PlanetEditorProviderImpl implements PlanetEditorProvider {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private PlanetPersistenceService persistenceService;

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
    public void createTerrainSlopePositions(int planetId, List<TerrainSlopePosition> createdSlopes) {
        try {
            persistenceService.createTerrainSlopePositions(planetId, createdSlopes);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateTerrainSlopePositions(int planetId, List<TerrainSlopePosition> updatedSlopes) {
        try {
            persistenceService.updateTerrainSlopePositions(planetId, updatedSlopes);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteTerrainSlopePositionIds(int planetId, List<Integer> deletedSlopeIds) {
        try {
            persistenceService.deleteTerrainSlopePositions(planetId, deletedSlopeIds);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}

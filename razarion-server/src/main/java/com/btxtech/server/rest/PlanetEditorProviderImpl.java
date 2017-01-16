package com.btxtech.server.rest;

import com.btxtech.server.persistence.PlanetPersistenceService;
import com.btxtech.shared.rest.PlanetEditorProvider;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
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
    public void createTerrainObjectPositions(List<TerrainObjectPosition> createdTerrainObjects) {
        try {
            persistenceService.createTerrainObjectPositions(createdTerrainObjects);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateTerrainObjectPositions(List<TerrainObjectPosition> updatedTerrainObjects) {
        try {
            persistenceService.updateTerrainObjectPositions(updatedTerrainObjects);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteTerrainObjectPositionIds(List<Integer> deletedTerrainIds) {
        try {
            persistenceService.deleteTerrainObjectPositionIds(deletedTerrainIds);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void createTerrainSlopePositions(List<TerrainSlopePosition> createdSlopes) {
        try {
            persistenceService.createTerrainSlopePositions(createdSlopes);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void updateTerrainSlopePositions(List<TerrainSlopePosition> updatedSlopes) {
        try {
            persistenceService.updateTerrainSlopePositions(updatedSlopes);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void deleteTerrainSlopePositionIds(List<Integer> deletedSlopeIds) {
        try {
            persistenceService.deleteTerrainSlopePositions(deletedSlopeIds);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}

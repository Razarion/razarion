package com.btxtech.server.rest;

import com.btxtech.server.persistence.PlanetPersistenceService;
import com.btxtech.shared.PlanetEditorProvider;
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
    @Transactional
    public void saveTerrainObjectPositions(List<TerrainObjectPosition> terrainObjectPositions) {
        try {
            persistenceService.saveTerrainObjectPositions(terrainObjectPositions);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void saveTerrainSlopePositions(List<TerrainSlopePosition> terrainSlopePositions) {
        try {
            persistenceService.saveTerrainSlopePositions(terrainSlopePositions);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}

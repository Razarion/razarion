package com.btxtech.server.persistence;

import com.btxtech.server.persistence.object.TerrainObjectPositionEntity;
import com.btxtech.server.persistence.surface.TerrainSlopePositionEntity;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 08.07.2016.
 */
@Singleton
public class PlanetPersistence {
    @Deprecated // Read from DB
    private static final int TUTORIAL_PLANET = 1;
    @Deprecated // Read from DB
    private static final int MULTIPLAYER_PLANET = 1;
    @Inject
    private TerrainElementPersistence terrainElementPersistence;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @SecurityCheck
    public int createPlanetConfig() {
        PlanetEntity planetEntity = new PlanetEntity();
        entityManager.persist(planetEntity);
        return planetEntity.getId();
    }

    @Transactional
    @SecurityCheck
    public void deletePlanetConfig(int planetId) {
        entityManager.remove(loadPlanet(planetId));
    }

    @Transactional
    @SecurityCheck
    public void createTerrainObjectPositions(int planetId, List<TerrainObjectPosition> createdTerrainObjects) {
        List<TerrainObjectPositionEntity> terrainObjectPositionEntities = new ArrayList<>();
        for (TerrainObjectPosition terrainObjectPosition : createdTerrainObjects) {
            TerrainObjectPositionEntity terrainObjectPositionEntity = new TerrainObjectPositionEntity();
            terrainObjectPositionEntity.setTerrainObjectEntity(terrainElementPersistence.getTerrainObjectEntity(terrainObjectPosition.getTerrainObjectId()));
            terrainObjectPositionEntity.setPosition(terrainObjectPosition.getPosition());
            terrainObjectPositionEntity.setScale(terrainObjectPosition.getScale());
            terrainObjectPositionEntity.setRotationZ(terrainObjectPosition.getRotationZ());
            terrainObjectPositionEntities.add(terrainObjectPositionEntity);
        }

        PlanetEntity planetEntity = loadPlanet(planetId);
        planetEntity.getTerrainObjectPositionEntities().addAll(terrainObjectPositionEntities);
        entityManager.persist(planetEntity);
    }

    @Transactional
    @SecurityCheck
    public void updateTerrainObjectPositions(int planetId, List<TerrainObjectPosition> updatedTerrainObjects) {
        PlanetEntity planetEntity = loadPlanet(planetId);
        for (TerrainObjectPosition terrainObjectPosition : updatedTerrainObjects) {
            TerrainObjectPositionEntity terrainObjectPositionEntity = getTerrainObjectPositionEntity(planetEntity, terrainObjectPosition.getId());
            terrainObjectPositionEntity.setTerrainObjectEntity(terrainElementPersistence.getTerrainObjectEntity(terrainObjectPosition.getTerrainObjectId()));
            terrainObjectPositionEntity.setPosition(terrainObjectPosition.getPosition());
            terrainObjectPositionEntity.setScale(terrainObjectPosition.getScale());
            terrainObjectPositionEntity.setRotationZ(terrainObjectPosition.getRotationZ());
        }
        entityManager.merge(planetEntity);
    }

    @Transactional
    @SecurityCheck
    public void deleteTerrainObjectPositionIds(int planetId, List<Integer> deletedTerrainIds) {
        PlanetEntity planetEntity = loadPlanet(planetId);
        for (int terrainSlopePositionId : deletedTerrainIds) {
            planetEntity.getTerrainObjectPositionEntities().remove(getTerrainObjectPositionEntity(planetEntity, terrainSlopePositionId));
        }
    }

    @Transactional
    @SecurityCheck
    public void updateTerrainSlopePositions(int planetId, List<TerrainSlopePosition> updatedSlopes) {
        PlanetEntity planetEntity = loadPlanet(planetId);
        for (TerrainSlopePosition terrainSlopePosition : updatedSlopes) {
            TerrainSlopePositionEntity terrainSlopePositionEntity = getSlopePositionEntityFromPlanet(planetEntity, terrainSlopePosition.getId());
            terrainSlopePositionEntity.setSlopeConfigEntity(terrainElementPersistence.getSlopeConfigEntity(terrainSlopePosition.getSlopeConfigEntity()));
            terrainSlopePositionEntity.getPolygon().clear();
            terrainSlopePositionEntity.getPolygon().addAll(terrainSlopePosition.getPolygon());
        }
        entityManager.merge(planetEntity);
    }

    @Transactional
    @SecurityCheck
    public void createTerrainSlopePositions(int planetId, Collection<TerrainSlopePosition> terrainSlopePositions) {
        List<TerrainSlopePositionEntity> terrainSlopePositionEntities = new ArrayList<>();
        for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
            TerrainSlopePositionEntity terrainSlopePositionEntity = new TerrainSlopePositionEntity();
            terrainSlopePositionEntity.setSlopeConfigEntity(terrainElementPersistence.getSlopeConfigEntity(terrainSlopePosition.getSlopeConfigEntity()));
            terrainSlopePositionEntity.setPolygon(terrainSlopePosition.getPolygon());
            terrainSlopePositionEntities.add(terrainSlopePositionEntity);
        }

        PlanetEntity planetEntity = loadPlanet(planetId);
        planetEntity.getTerrainSlopePositionEntities().addAll(terrainSlopePositionEntities);
        entityManager.persist(planetEntity);
    }

    @Transactional
    @SecurityCheck
    public void deleteTerrainSlopePositions(int planetId, Collection<Integer> terrainSlopePositionIds) {
        PlanetEntity planetEntity = loadPlanet(planetId);
        for (int terrainSlopePositionId : terrainSlopePositionIds) {
            planetEntity.getTerrainSlopePositionEntities().remove(getSlopePositionEntityFromPlanet(planetEntity, terrainSlopePositionId));
        }
    }

    @Transactional
    public PlanetConfig readTutorialPlanetConfig() {
        return loadPlanet(TUTORIAL_PLANET).toPlanetConfig();
    }

    @Transactional
    public PlanetConfig readMultiplayerPlanetConfig() {
        return loadPlanet(MULTIPLAYER_PLANET).toPlanetConfig();
    }

    private TerrainSlopePositionEntity getSlopePositionEntityFromPlanet(PlanetEntity planetEntity, int id) {
        for (TerrainSlopePositionEntity terrainSlopePositionEntity : planetEntity.getTerrainSlopePositionEntities()) {
            if (terrainSlopePositionEntity.getId() == id) {
                return terrainSlopePositionEntity;
            }
        }
        throw new IllegalArgumentException("No TerrainSlopePositionEntity on planet for id: " + id);
    }

    private TerrainObjectPositionEntity getTerrainObjectPositionEntity(PlanetEntity planetEntity, int id) {
        for (TerrainObjectPositionEntity terrainObjectPositionEntity : planetEntity.getTerrainObjectPositionEntities()) {
            if (terrainObjectPositionEntity.getId() == id) {
                return terrainObjectPositionEntity;
            }
        }
        throw new IllegalArgumentException("No TerrainObjectPositionEntity on planet for id: " + id);
    }

    @SecurityCheck
    public PlanetEntity loadPlanet(int planetId) {
        PlanetEntity planetEntity = entityManager.find(PlanetEntity.class, planetId);
        if (planetEntity == null) {
            throw new IllegalArgumentException("No planet for id: " + planetId);
        }
        return planetEntity;
    }
}
package com.btxtech.server.persistence;

import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.server.persistence.object.TerrainObjectPositionEntity;
import com.btxtech.server.persistence.surface.TerrainSlopePositionEntity;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 08.07.2016.
 */
@Singleton
public class PlanetPersistenceService {
    @Inject
    private TerrainElementPersistence terrainElementPersistence;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void saveTerrainObjectPositions(List<TerrainObjectPosition> terrainObjectPositions) {
        List<TerrainObjectPositionEntity> terrainObjectPositionEntities = new ArrayList<>();
        for (TerrainObjectPosition objectPosition : terrainObjectPositions) {
            TerrainObjectPositionEntity terrainObjectPositionEntity = new TerrainObjectPositionEntity();
            terrainObjectPositionEntity.fromTerrainObjectPosition(objectPosition, entityManager.find(TerrainObjectEntity.class, (long) objectPosition.getTerrainObjectId()));
            terrainObjectPositionEntities.add(terrainObjectPositionEntity);
        }
        PlanetEntity planetEntity = loadPlanet();
        planetEntity.setTerrainObjectPositionEntities(terrainObjectPositionEntities);
        entityManager.merge(planetEntity);
    }

    @Transactional
    public void updateTerrainSlopePositions(List<TerrainSlopePosition> updatedSlopes) {
        PlanetEntity planetEntity = loadPlanet();
        for (TerrainSlopePosition terrainSlopePosition : updatedSlopes) {
            TerrainSlopePositionEntity terrainSlopePositionEntity = getSlopePositionEntityFromPlanet(planetEntity, terrainSlopePosition.getId());
            terrainSlopePositionEntity.setSlopeConfigEntity(terrainElementPersistence.getSlopeConfigEntity(terrainSlopePosition.getSlopeId()));
            terrainSlopePositionEntity.getPolygon().clear();
            terrainSlopePositionEntity.getPolygon().addAll(terrainSlopePosition.getPolygon());
        }
        entityManager.merge(planetEntity);
    }

    @Transactional
    public void createTerrainSlopePositions(Collection<TerrainSlopePosition> terrainSlopePositions) {
        List<TerrainSlopePositionEntity> terrainSlopePositionEntities = new ArrayList<>();
        for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
            TerrainSlopePositionEntity terrainSlopePositionEntity = new TerrainSlopePositionEntity();
            terrainSlopePositionEntity.setSlopeConfigEntity(terrainElementPersistence.getSlopeConfigEntity(terrainSlopePosition.getSlopeId()));
            terrainSlopePositionEntity.setPolygon(terrainSlopePosition.getPolygon());
            terrainSlopePositionEntities.add(terrainSlopePositionEntity);
        }

        PlanetEntity planetEntity = loadPlanet();
        planetEntity.getTerrainSlopePositionEntities().addAll(terrainSlopePositionEntities);
        entityManager.persist(planetEntity);
    }

    @Transactional
    public void deleteTerrainSlopePositions(Collection<Integer> terrainSlopePositionIds) {
        PlanetEntity planetEntity = loadPlanet();
        for (int terrainSlopePositionId : terrainSlopePositionIds) {
            planetEntity.getTerrainSlopePositionEntities().remove(getSlopePositionEntityFromPlanet(planetEntity, terrainSlopePositionId));
        }
    }

    private TerrainSlopePositionEntity getSlopePositionEntityFromPlanet(PlanetEntity planetEntity, int id) {
        for (TerrainSlopePositionEntity terrainSlopePositionEntity : planetEntity.getTerrainSlopePositionEntities()) {
            if (terrainSlopePositionEntity.getId() == id) {
                return terrainSlopePositionEntity;
            }
        }
        throw new IllegalArgumentException("No TerrainSlopePositionEntity on planet for id: " + id);
    }

    private PlanetEntity loadPlanet() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<PlanetEntity> userQuery = criteriaBuilder.createQuery(PlanetEntity.class);
        Root<PlanetEntity> from = userQuery.from(PlanetEntity.class);
        CriteriaQuery<PlanetEntity> userSelect = userQuery.select(from);
        return entityManager.createQuery(userSelect).getSingleResult();
    }
}

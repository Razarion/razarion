package com.btxtech.server.persistence;

import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.server.persistence.object.TerrainObjectPositionEntity;
import com.btxtech.server.persistence.object.TerrainObjectPositionEntity_;
import com.btxtech.server.persistence.surface.SlopeConfigEntity;
import com.btxtech.server.persistence.surface.TerrainSlopePositionEntity;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
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
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void saveTerrainObjectPositions(List<TerrainObjectPosition> terrainObjectPositions) {
        List<TerrainObjectPositionEntity> terrainObjectPositionEntities = new ArrayList<>();
        for (TerrainObjectPosition objectPosition : terrainObjectPositions) {
            TerrainObjectPositionEntity terrainSlopePositionEntity = new TerrainObjectPositionEntity();
            terrainSlopePositionEntity.fromTerrainObjectPosition(objectPosition, entityManager.find(TerrainObjectEntity.class, (long) objectPosition.getTerrainObjectId()));
            terrainObjectPositionEntities.add(terrainSlopePositionEntity);
        }
        PlanetEntity planetEntity = loadPlanet();
        planetEntity.setTerrainObjectPositionEntities(terrainObjectPositionEntities);
        entityManager.merge(planetEntity);
    }

    @Transactional
    public void saveTerrainSlopePositions(Collection<TerrainSlopePosition> terrainSlopePositions) {
        List<TerrainSlopePositionEntity> terrainSlopePositionEntities = new ArrayList<>();
        for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
            TerrainSlopePositionEntity terrainSlopePositionEntity = new TerrainSlopePositionEntity();
            terrainSlopePositionEntity.fromTerrainSlopePosition(terrainSlopePosition, entityManager.find(SlopeConfigEntity.class, (long) terrainSlopePosition.getSlopeId()));
            terrainSlopePositionEntities.add(terrainSlopePositionEntity);
        }

        PlanetEntity planetEntity = loadPlanet();
        planetEntity.setTerrainSlopePositionEntities(terrainSlopePositionEntities);
        entityManager.merge(planetEntity);
    }

    private Collection<Long> getTerrainObjectPositionIds() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<TerrainObjectPositionEntity> root = cq.from(TerrainObjectPositionEntity.class);
        cq.multiselect(root.get(TerrainObjectPositionEntity_.id));
        List<Tuple> tupleResult = entityManager.createQuery(cq).getResultList();
        Collection<Long> ids = new ArrayList<>();
        for (Tuple t : tupleResult) {
            ids.add((Long) t.get(0));
        }
        return ids;
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

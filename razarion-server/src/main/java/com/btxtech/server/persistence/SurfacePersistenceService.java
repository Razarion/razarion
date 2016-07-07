package com.btxtech.server.persistence;

import com.btxtech.server.collada.ColladaConverter;
import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.persistence.surface.SlopeConfigEntity;
import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.shared.dto.GroundSkeleton;
import com.btxtech.shared.dto.SlopeSkeleton;
import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
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
 * 06.07.2016.
 */
public class SurfacePersistenceService {
    @Inject
    private ExceptionHandler exceptionHandler;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<SlopeSkeleton> loadSlopeSkeletons() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<SlopeConfigEntity> userQuery = criteriaBuilder.createQuery(SlopeConfigEntity.class);
            Root<SlopeConfigEntity> root = userQuery.from(SlopeConfigEntity.class);
            CriteriaQuery<SlopeConfigEntity> userSelect = userQuery.select(root);
            Collection<SlopeConfigEntity> slopeConfigEntities = entityManager.createQuery(userSelect).getResultList();

            List<SlopeSkeleton> slopeSkeletons = new ArrayList<>();
            for (SlopeConfigEntity slopeConfigEntity : slopeConfigEntities) {
                slopeSkeletons.add(slopeConfigEntity.toSlopeSkeleton());
            }

            return slopeSkeletons;
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Transactional
    public GroundSkeleton loadGroundSkeleton() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
            Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
            CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
            return entityManager.createQuery(userSelect).getSingleResult().generateGroundSkeleton();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Transactional
    public List<TerrainObject> loadTerrainObjects() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<TerrainObjectEntity> userQuery = criteriaBuilder.createQuery(TerrainObjectEntity.class);
            Root<TerrainObjectEntity> from = userQuery.from(TerrainObjectEntity.class);
            CriteriaQuery<TerrainObjectEntity> userSelect = userQuery.select(from);
            List<TerrainObjectEntity> terrainObjectEntities = entityManager.createQuery(userSelect).getResultList();

            List<TerrainObject> terrainObjects = new ArrayList<>();
            for (TerrainObjectEntity terrainObjectEntity : terrainObjectEntities) {
                terrainObjects.add(ColladaConverter.convertToTerrainObject(terrainObjectEntity));
            }
            return terrainObjects;

        } catch (RuntimeException e) {
            exceptionHandler.handleException(e);
            throw e;
        } catch (Exception e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

}

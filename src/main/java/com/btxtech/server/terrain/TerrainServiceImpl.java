package com.btxtech.server.terrain;

import com.btxtech.server.ExceptionHandler;
import com.btxtech.server.collada.ColladaConverter;
import com.btxtech.server.terrain.object.TerrainObjectEntity;
import com.btxtech.server.terrain.object.TerrainObjectPositionEntity;
import com.btxtech.server.terrain.surface.GroundConfigEntity;
import com.btxtech.server.terrain.surface.SlopeConfigEntity;
import com.btxtech.server.terrain.surface.TerrainSlopePositionEntity;
import com.btxtech.shared.TerrainService;
import com.btxtech.shared.dto.GroundSkeleton;
import com.btxtech.shared.dto.SlopeSkeleton;
import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.google.gson.Gson;
import org.jboss.errai.bus.server.annotations.Service;

import javax.enterprise.context.ApplicationScoped;
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
 * 24.04.2016.
 */
@Service
@ApplicationScoped
public class TerrainServiceImpl implements TerrainService {
    @Inject
    private ExceptionHandler exceptionHandler;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Collection<SlopeSkeleton> loadSlopeSkeletons() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<SlopeConfigEntity> userQuery = criteriaBuilder.createQuery(SlopeConfigEntity.class);
            Root<SlopeConfigEntity> root = userQuery.from(SlopeConfigEntity.class);
            CriteriaQuery<SlopeConfigEntity> userSelect = userQuery.select(root);
            Collection<SlopeConfigEntity> slopeConfigEntities = entityManager.createQuery(userSelect).getResultList();

            Gson gson = new Gson();
            Collection<SlopeSkeleton> slopeSkeletons = new ArrayList<>();
            for (SlopeConfigEntity slopeConfigEntity : slopeConfigEntities) {
                SlopeSkeleton slopeSkeleton = slopeConfigEntity.toSlopeSkeleton();
                slopeSkeletons.add(slopeSkeleton);
                System.out.println("loadSlopeSkeletons --------------------------------------------------------");
                String json = gson.toJson(slopeSkeleton);
                System.out.println(json);
                System.out.println("--------------------------------------------------------");
            }

            return slopeSkeletons;
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public GroundSkeleton loadGroundSkeleton() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
            Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
            CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
            GroundConfigEntity groundSkeletonEntity = entityManager.createQuery(userSelect).getSingleResult();
            System.out.println("loadGroundSkeleton --------------------------------------------------------");
            Gson gson = new Gson();
            String json = gson.toJson(groundSkeletonEntity.generateGroundSkeleton());
            System.out.println(json);
            System.out.println("--------------------------------------------------------");

            return groundSkeletonEntity.generateGroundSkeleton();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public Collection<TerrainSlopePosition> loadTerrainSlopePositions() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<TerrainSlopePositionEntity> userQuery = criteriaBuilder.createQuery(TerrainSlopePositionEntity.class);
            Root<TerrainSlopePositionEntity> from = userQuery.from(TerrainSlopePositionEntity.class);
            CriteriaQuery<TerrainSlopePositionEntity> userSelect = userQuery.select(from);
            List<TerrainSlopePositionEntity> terrainSlopePositionEntities = entityManager.createQuery(userSelect).getResultList();
            Collection<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
            for (TerrainSlopePositionEntity terrainSlopePositionEntity : terrainSlopePositionEntities) {
                terrainSlopePositions.add(terrainSlopePositionEntity.generateTerrainSlopePosition());
            }
            System.out.println("TerrainSlopePosition------------------------------------");
            Gson gson = new Gson();
            String json = gson.toJson(terrainSlopePositions);
            System.out.println(json);
            System.out.println("--------------------------------------------------------");
            return terrainSlopePositions;
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public Collection<TerrainObject> loadTerrainObjects() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<TerrainObjectEntity> userQuery = criteriaBuilder.createQuery(TerrainObjectEntity.class);
            Root<TerrainObjectEntity> from = userQuery.from(TerrainObjectEntity.class);
            CriteriaQuery<TerrainObjectEntity> userSelect = userQuery.select(from);
            List<TerrainObjectEntity> terrainObjectEntities = entityManager.createQuery(userSelect).getResultList();

            Collection<TerrainObject> terrainObjects = new ArrayList<>();
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

    @Override
    @Transactional
    public Collection<TerrainObjectPosition> loadTerrainObjectPositions() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<TerrainObjectPositionEntity> userQuery = criteriaBuilder.createQuery(TerrainObjectPositionEntity.class);
            Root<TerrainObjectPositionEntity> from = userQuery.from(TerrainObjectPositionEntity.class);
            CriteriaQuery<TerrainObjectPositionEntity> userSelect = userQuery.select(from);
            List<TerrainObjectPositionEntity> terrainObjectPositionEntities = entityManager.createQuery(userSelect).getResultList();

            Collection<TerrainObjectPosition> objectPositions = new ArrayList<>();
            for (TerrainObjectPositionEntity terrainObjectPositionEntity : terrainObjectPositionEntities) {
                TerrainObjectPosition objectPosition = new TerrainObjectPosition();
                objectPosition.setId(terrainObjectPositionEntity.getId().intValue());
                objectPosition.setTerrainObjectId(terrainObjectPositionEntity.getTerrainObjectEntity().getId().intValue());
                objectPosition.setScale(terrainObjectPositionEntity.getScale());
                objectPosition.setZRotation(terrainObjectPositionEntity.getZRotation());
                objectPosition.setPosition(terrainObjectPositionEntity.getPosition());
                objectPositions.add(objectPosition);
            }
            return objectPositions;
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

}

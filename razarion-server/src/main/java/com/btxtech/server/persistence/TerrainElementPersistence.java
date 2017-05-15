package com.btxtech.server.persistence;

import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.server.persistence.object.TerrainObjectEntity_;
import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.persistence.surface.SlopeConfigEntity;
import com.btxtech.server.persistence.surface.SlopeConfigEntity_;
import com.btxtech.server.persistence.surface.WaterConfigEntity;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Singleton
public class TerrainElementPersistence {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private Shape3DPersistence shape3DPersistence;
    @Inject
    private ImagePersistence imagePersistence;

    @Transactional
    public GroundSkeletonConfig loadGroundSkeleton() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
        Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
        CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
        return entityManager.createQuery(userSelect).getSingleResult().generateGroundSkeleton();
    }

    @Transactional
    public WaterConfig readWaterConfig() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<WaterConfigEntity> userQuery = criteriaBuilder.createQuery(WaterConfigEntity.class);
        Root<WaterConfigEntity> from = userQuery.from(WaterConfigEntity.class);
        CriteriaQuery<WaterConfigEntity> userSelect = userQuery.select(from);
        return entityManager.createQuery(userSelect).getSingleResult().toWaterConfig();
    }

    @Transactional
    public GroundConfig loadGroundConfig() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
        Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
        CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
        return entityManager.createQuery(userSelect).getSingleResult().toGroundConfig();
    }

    @Transactional
    @SecurityCheck
    public GroundConfig saveGroundConfig(GroundConfig groundConfig) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
        Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
        CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
        GroundConfigEntity groundConfigEntity = entityManager.createQuery(userSelect).getSingleResult();
        groundConfigEntity.fromGroundConfig(groundConfig, imagePersistence);
        return entityManager.merge(groundConfigEntity).toGroundConfig();
    }

    @Transactional
    public List<ObjectNameId> getSlopeNameIds() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<SlopeConfigEntity> root = cq.from(SlopeConfigEntity.class);
        cq.multiselect(root.get(SlopeConfigEntity_.id), root.get(SlopeConfigEntity_.internalName));
        List<Tuple> tupleResult = entityManager.createQuery(cq).getResultList();
        return tupleResult.stream().map(t -> new ObjectNameId(((int) t.get(0)), (String) t.get(1))).collect(Collectors.toList());
    }

    @Transactional
    @SecurityCheck
    public SlopeConfig createSlopeConfig() {
        SlopeConfigEntity slopeConfigEntity = new SlopeConfigEntity();
        slopeConfigEntity.setDefault();
        entityManager.persist(slopeConfigEntity);
        return slopeConfigEntity.toSlopeConfig();
    }

    @Transactional
    public SlopeConfig readSlopeConfig(int id) {
        return entityManager.find(SlopeConfigEntity.class, id).toSlopeConfig();
    }

    @Transactional
    public List<SlopeConfig> readSlopeConfigs() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SlopeConfigEntity> userQuery = criteriaBuilder.createQuery(SlopeConfigEntity.class);
        Root<SlopeConfigEntity> root = userQuery.from(SlopeConfigEntity.class);
        CriteriaQuery<SlopeConfigEntity> userSelect = userQuery.select(root);
        Collection<SlopeConfigEntity> slopeConfigEntities = entityManager.createQuery(userSelect).getResultList();

        return slopeConfigEntities.stream().map(SlopeConfigEntity::toSlopeConfig).collect(Collectors.toList());
    }

    @Transactional
    public List<SlopeSkeletonConfig> loadSlopeSkeletons() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SlopeConfigEntity> userQuery = criteriaBuilder.createQuery(SlopeConfigEntity.class);
        Root<SlopeConfigEntity> root = userQuery.from(SlopeConfigEntity.class);
        CriteriaQuery<SlopeConfigEntity> userSelect = userQuery.select(root);
        Collection<SlopeConfigEntity> slopeConfigEntities = entityManager.createQuery(userSelect).getResultList();

        return slopeConfigEntities.stream().map(SlopeConfigEntity::toSlopeSkeleton).collect(Collectors.toList());
    }

    @Transactional
    @SecurityCheck
    public void updateSlopeConfig(SlopeConfig slopeConfig) {
        SlopeConfigEntity slopeConfigEntity = entityManager.find(SlopeConfigEntity.class, slopeConfig.getId());
        slopeConfigEntity.fromSlopeConfig(slopeConfig, imagePersistence);
        entityManager.merge(slopeConfigEntity);
    }

    @Transactional
    @SecurityCheck
    public void deleteSlopeConfig(int id) {
        SlopeConfigEntity slopeConfigEntity = entityManager.find(SlopeConfigEntity.class, id);
        entityManager.remove(slopeConfigEntity);
    }

    @Transactional
    public List<ObjectNameId> getTerrainObjectNameIds() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<TerrainObjectEntity> root = cq.from(TerrainObjectEntity.class);
        cq.multiselect(root.get(TerrainObjectEntity_.id), root.get(TerrainObjectEntity_.internalName));
        List<Tuple> tupleResult = entityManager.createQuery(cq).getResultList();
        return tupleResult.stream().map(t -> new ObjectNameId((int) t.get(0), (String) t.get(1))).collect(Collectors.toList());
    }

    @Transactional
    public TerrainObjectConfig readTerrainObjectConfig(int id) {
        return entityManager.find(TerrainObjectEntity.class, id).toTerrainObjectConfig();
    }

    @Transactional
    public List<TerrainObjectConfig> readTerrainObjects() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<TerrainObjectEntity> userQuery = criteriaBuilder.createQuery(TerrainObjectEntity.class);
        Root<TerrainObjectEntity> from = userQuery.from(TerrainObjectEntity.class);
        CriteriaQuery<TerrainObjectEntity> userSelect = userQuery.select(from);
        List<TerrainObjectEntity> terrainObjectEntities = entityManager.createQuery(userSelect).getResultList();

        return terrainObjectEntities.stream().map(TerrainObjectEntity::toTerrainObjectConfig).collect(Collectors.toList());
    }

    @Transactional
    @SecurityCheck
    public void saveTerrainObject(TerrainObjectConfig terrainObjectConfig) {
        TerrainObjectEntity terrainObjectEntity = entityManager.find(TerrainObjectEntity.class, terrainObjectConfig.getId());
        terrainObjectEntity.fromTerrainObjectConfig(terrainObjectConfig, shape3DPersistence.getColladaEntity(terrainObjectConfig.getShape3DId()));
    }

    @Transactional
    @SecurityCheck
    public void deleteTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig) {
        TerrainObjectEntity terrainObjectEntity = entityManager.find(TerrainObjectEntity.class, terrainObjectConfig.getId());
        entityManager.remove(terrainObjectEntity);
    }

    @Transactional
    @SecurityCheck
    public TerrainObjectConfig createTerrainObjectConfig() {
        TerrainObjectEntity terrainObjectEntity = new TerrainObjectEntity();
        entityManager.persist(terrainObjectEntity);
        return terrainObjectEntity.toTerrainObjectConfig();
    }

    @Transactional
    public SlopeConfigEntity getSlopeConfigEntity(long slopeId) {
        return entityManager.find(SlopeConfigEntity.class, slopeId);
    }

    @Transactional
    public TerrainObjectEntity getTerrainObjectEntity(long terrainObjectId) {
        return entityManager.find(TerrainObjectEntity.class, terrainObjectId);
    }
}
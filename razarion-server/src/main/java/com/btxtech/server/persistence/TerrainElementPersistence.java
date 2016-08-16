package com.btxtech.server.persistence;

import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.server.persistence.object.TerrainObjectEntity_;
import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.persistence.surface.SlopeConfigEntity;
import com.btxtech.server.persistence.surface.SlopeConfigEntity_;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 06.07.2016.
 */
public class TerrainElementPersistence {
    @PersistenceContext
    private EntityManager entityManager;

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
    public GroundConfig loadGroundConfig() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
        Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
        CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
        return entityManager.createQuery(userSelect).getSingleResult().toGroundConfig();
    }

    @Transactional
    public GroundConfig saveGroundConfig(GroundConfig groundConfig) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
        Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
        CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
        GroundConfigEntity groundConfigEntity = entityManager.createQuery(userSelect).getSingleResult();
        groundConfigEntity.fromGroundConfig(groundConfig);
        return entityManager.merge(groundConfigEntity).toGroundConfig();
    }

    @Transactional
    public List<ObjectNameId> getSlopeNameIds() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<SlopeConfigEntity> root = cq.from(SlopeConfigEntity.class);
        cq.multiselect(root.get(SlopeConfigEntity_.id), root.get(SlopeConfigEntity_.internalName));
        List<Tuple> tupleResult = entityManager.createQuery(cq).getResultList();
        return tupleResult.stream().map(t -> new ObjectNameId(((Long) t.get(0)).intValue(), (String) t.get(1))).collect(Collectors.toList());
    }

    @Transactional
    public SlopeConfig loadSlopeConfig(int id) {
        return entityManager.find(SlopeConfigEntity.class, (long) id).toSlopeConfig();
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
    public SlopeConfig saveSlopeConfig(SlopeConfig slopeConfig) {
        SlopeConfigEntity slopeConfigEntity;
        if (slopeConfig.hasId()) {
            slopeConfigEntity = entityManager.find(SlopeConfigEntity.class, (long) slopeConfig.getId());
        } else {
            slopeConfigEntity = new SlopeConfigEntity();
        }
        slopeConfigEntity.fromSlopeConfig(slopeConfig);

        return entityManager.merge(slopeConfigEntity).toSlopeConfig();
    }

    @Transactional
    public void deleteSlopeConfig(SlopeConfig slopeConfig) {
        SlopeConfigEntity slopeConfigEntity = entityManager.find(SlopeConfigEntity.class, (long) slopeConfig.getId());
        entityManager.remove(slopeConfigEntity);
    }

    @Transactional
    public List<ObjectNameId> getTerrainObjectNameIds() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<TerrainObjectEntity> root = cq.from(TerrainObjectEntity.class);
        cq.multiselect(root.get(TerrainObjectEntity_.id), root.get(TerrainObjectEntity_.internalName));
        List<Tuple> tupleResult = entityManager.createQuery(cq).getResultList();
        return tupleResult.stream().map(t -> new ObjectNameId(((Long) t.get(0)).intValue(), (String) t.get(1))).collect(Collectors.toList());
    }

    @Transactional
    public TerrainObjectConfig loadTerrainObjectConfig(int id) {
        return entityManager.find(TerrainObjectEntity.class, (long) id).toTerrainObjectConfig();
    }

    @Transactional
    public List<TerrainObjectConfig> loadTerrainObjects() throws ParserConfigurationException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<TerrainObjectEntity> userQuery = criteriaBuilder.createQuery(TerrainObjectEntity.class);
        Root<TerrainObjectEntity> from = userQuery.from(TerrainObjectEntity.class);
        CriteriaQuery<TerrainObjectEntity> userSelect = userQuery.select(from);
        List<TerrainObjectEntity> terrainObjectEntities = entityManager.createQuery(userSelect).getResultList();

        return terrainObjectEntities.stream().map(TerrainObjectEntity::toTerrainObjectConfig).collect(Collectors.toList());
    }

    @Transactional
    public TerrainObjectConfig saveTerrainObject(TerrainObjectConfig terrainObjectConfig) {
        TerrainObjectEntity terrainObjectEntity;
        if (terrainObjectConfig.hasId()) {
            terrainObjectEntity = entityManager.find(TerrainObjectEntity.class, (long) terrainObjectConfig.getId());
        } else {
            terrainObjectEntity = new TerrainObjectEntity();
        }
        ColladaEntity colladaEntity = null;
        if (terrainObjectConfig.getShape3DId() != null) {
            colladaEntity = entityManager.find(ColladaEntity.class, terrainObjectConfig.getShape3DId().longValue());
        }
        terrainObjectEntity.fromTerrainObjectConfig(terrainObjectConfig, colladaEntity);

        return entityManager.merge(terrainObjectEntity).toTerrainObjectConfig();
    }

    @Transactional
    public void deleteTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig) {
        TerrainObjectEntity terrainObjectEntity = entityManager.find(TerrainObjectEntity.class, (long) terrainObjectConfig.getId());
        entityManager.remove(terrainObjectEntity);
    }
}